package com.github.ledandre.bufferz.domain.automatic;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ledandre.bufferz.domain.Buffer;

public class AutomaticExecutionBuffer <T> implements Buffer<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticExecutionBuffer.class);

    public final int sizeLimit;
    private Queue<T> buffer = new LinkedList<>();
    private Function<Collection<T>, Collection<T>> execution;
    private Map<Class<? extends Exception>, Callable<?>> exceptionHandler;

    @SuppressWarnings("unchecked")
    public AutomaticExecutionBuffer(Function<?, ?> execution, int sizeLimit, Map<Class<? extends Exception>, Callable<?>> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        this.execution = (Function<Collection<T>, Collection<T>>) execution;
        this.sizeLimit = sizeLimit;
    }

    @Override
    public void add(T object) {
        buffer.add(object);
        checkBufferSize();
    }

    private void checkBufferSize() {
        if (buffer.size() < sizeLimit) return;

        triggerExecution();
    }

    @Override
    public Collection<T> retrieveData() {
        if (buffer.isEmpty()) return Collections.emptyList();

        Collection<T> objects = new ArrayList<>();
        while(!buffer.isEmpty()) {
            objects.add(buffer.poll());
        }
        return objects;
    }

    @Override
    public void drain() {
        if (buffer.isEmpty()) return;
        triggerExecution();
    }

    private void triggerExecution() {
        try {
            execution.apply(retrieveData());

        } catch (Exception e) {
            Optional<Callable<?>> exceptionHandling;
            Class<?> exception = e.getClass();

            do {
                exceptionHandling = ofNullable(exceptionHandler.get(exception));
                if (exceptionHandling.isPresent()) {
                    callHandler(exceptionHandling.get(), e);
                    return;
                }
                exception = exception.getSuperclass();

            } while (exception != Object.class);

            LOGGER.error("Ocorreu uma exceção ({}) que não foi tratada e será ignorada.: ", e.getClass(), e);
        }
    }

    private void callHandler(Callable<?> exceptionHandling, Exception exception) {
        try {
            exceptionHandling.call();
        } catch (Exception e1) {
            LOGGER.error("Não foi possível tratar a exceção {} - {}", exception.getClass(), exception.getMessage(), e1);
        }
    }
}

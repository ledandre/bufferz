package eu.leandro.andre.domain.automatic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class AutomaticExecutionBufferBuilder <T> {
    private final Function<?, ?> execution;
    private int sizeLimit;
    private Map<Class<? extends Exception>, Callable<?>> exceptionHandler;

    public AutomaticExecutionBufferBuilder<T> whenBufferSizeIs(int size) {
        this.sizeLimit = size;
        this.exceptionHandler = new HashMap<>();
        return this;
    }

    public static <T> AutomaticExecutionBufferBuilder<T> execute(Function<Collection<T>, ?> execution) {
        return new AutomaticExecutionBufferBuilder<T>(execution);
    }

    private AutomaticExecutionBufferBuilder(Function<?, ?> execution) {
        this.execution = execution;
    }

    public AutomaticExecutionBuffer<T> build() {
        return new AutomaticExecutionBuffer<T>(execution, sizeLimit, exceptionHandler);
    }

    public ExceptionHandler handling(Class<? extends Exception> exceptionClass) {
        return new ExceptionHandler(exceptionClass,this);
    }

    public class ExceptionHandler{
        private Class<? extends Exception> exceptionClass;
        private AutomaticExecutionBufferBuilder<T> builder;

        private ExceptionHandler(Class<? extends Exception> exceptionClass, AutomaticExecutionBufferBuilder<T> automaticExecutionBufferBuilder) {
            this.exceptionClass = exceptionClass;
            builder = automaticExecutionBufferBuilder;
        }

        public AutomaticExecutionBufferBuilder<T> with(Callable<?> function) {
            builder.exceptionHandler.put(exceptionClass, function);
            return builder;
        }

        public AutomaticExecutionBufferBuilder<T> with(Runnable function) {
            return with(Executors.callable(function));
        }
    }
}

package eu.leandro.andre.domain;

import java.util.Collection;

public interface Buffer <T> {

    public void add(T t);

    /**
     * Get data and clear buffer.
     * The buffer will be empty after the invocation of this method.
     * @return A collection of <T>
     */
    public Collection<T> retrieveData();

    /**
     * Drain out the buffer executing the pre defined execution.
     */
    void drain();
}

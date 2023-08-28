package com.lls.core.disruptor;

/**
 * 监听接口
 * @param <E>
 */
public interface EventListener<E> {
    void onEvent(E event);

    void onException(Throwable ex,long sequence,E event);
}


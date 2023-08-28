package com.lls.core.disruptor;

/**
 * 多生产者多消费者处理接口
 * @param <E>
 */
public interface ParallelQueue<E> {

    /**
     * 添加事件/添加多个事件
     * @param event
     */
    void add(E event);
    void add(E... event);

    /**
     * 尝试添加事件/添加多个事件
     * @param event
     * @return
     */
    boolean tryAdd(E event);
    boolean tryAdd(E... event);

    /**
     * 启动
     * 启动Disruptor（其是一个高性能的，可用于并行计算场景的无锁环形队列），同时可以启动工作线程，此时队里就会变成可用状态
     * 这时候就可以对队列进行添加事件处理并且工作线程可以对事件进行消费。
     */
    void start();

    /**
     * 销毁
     * 关闭Disruptor
     */
    void shutDown();

    /**
     * 判断是否已经销毁
     */
    boolean isShutDown();

}



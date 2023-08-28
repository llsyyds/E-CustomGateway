package com.lls.core.context;

/**
 * 核心上下文接口定义
 */
public interface IContext {

    int RUNNING = 0;

    int WRITTEN = 1;

    int COMPLETED = 2;

    int TERMINATED = -1;

    void running();

    void written();

    void completed();

    void terminated();

    boolean isRunning();

    boolean isWritten();

    boolean isCompleted();

    boolean isTerminated();
}

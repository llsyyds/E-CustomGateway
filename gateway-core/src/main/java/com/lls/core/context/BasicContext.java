package com.lls.core.context;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 核心上下文基础类
 */
public class BasicContext implements IContext{

    @Getter
    protected final String protocol;

    protected volatile int status  = RUNNING;

    @Getter
    protected final ChannelHandlerContext nettyCtx;

    protected  final Map<String,Object> attributes = new HashMap<String,Object>();

    @Getter
    @Setter
    protected Throwable throwable;

    @Getter
    @Setter
    protected final boolean keepAlive;

    protected final AtomicBoolean requestReleased = new AtomicBoolean(false);

    protected List<Consumer<IContext>> completedCallbacks;

    public BasicContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive) {
        this.protocol = protocol;
        this.nettyCtx = nettyCtx;
        this.keepAlive = keepAlive;
    }

    @Override
    public void running() {
        status = RUNNING;
    }

    @Override
    public void written() {
        status = WRITTEN;
    }

    @Override
    public void completed() {
        status = COMPLETED;
    }

    @Override
    public void terminated() {
        status = TERMINATED;
    }

    @Override
    public boolean isRunning() {
        return status == RUNNING;
    }

    @Override
    public boolean isWritten() {
        return  status == WRITTEN;
    }

    @Override
    public boolean isCompleted() {
        return status == COMPLETED;
    }

    @Override
    public boolean isTerminated() {
        return status == TERMINATED;
    }



    public Object getAttribute(Map<String, Object> key) {
        return attributes.get(key);
    }

    public void setAttribute(String key,Object obj) {
        attributes.put(key,obj);
    }

    public void releaseRequest() {
        this.requestReleased.compareAndSet(false,true);
    }

    public void setCompletedCallBack(Consumer<IContext> consumer) {
        if(completedCallbacks == null){
            completedCallbacks = new ArrayList<>();
        }
        completedCallbacks.add(consumer);
    }

    public void invokeCompletedCallBack() {
        if(completedCallbacks == null){
            completedCallbacks.forEach(call->call.accept(this));
        }
    }
}

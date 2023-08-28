package com.lls.core.context;


import com.lls.common.config.Rule;
import com.lls.core.request.GatewayRequest;
import com.lls.core.response.GatewayResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.Setter;

import io.micrometer.core.instrument.Timer;


/**
 * 网关核心上下文
 */
@Getter
@Setter
public class GatewayContext extends BasicContext{

    private GatewayRequest request;

    private GatewayResponse response;

    public void setResponse(Object response) {
        this.response = (GatewayResponse) response;
    }

    private Rule rule;

    private int currentRetryTimes;

    private boolean gray;

    private Timer.Sample timerSample;

    public static class Builder {
        private String protocol;
        private ChannelHandlerContext nettyCtx;
        private boolean keepAlive;
        private GatewayRequest request;
        private Rule rule;
        private int currentRetryTimes ;
        private Timer.Sample timerSample;
        private boolean gray;

        Builder() {
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder nettyCtx(ChannelHandlerContext nettyCtx) {
            this.nettyCtx = nettyCtx;
            return this;
        }

        public Builder keepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public Builder request(GatewayRequest request) {
            this.request = request;
            return this;
        }

        public Builder rule(Rule rule) {
            this.rule = rule;
            return this;
        }

        public Builder currentRetryTimes(int currentRetryTimes) {
            this.currentRetryTimes = currentRetryTimes;
            return this;
        }

        public Builder gray(boolean gray) {
            this.gray = gray;
            return this;
        }

        public Builder timerSample(Timer.Sample timerSample) {
            this.timerSample = timerSample;
            return this;
        }

        public GatewayContext build() {
            return new GatewayContext(protocol, nettyCtx, keepAlive, request, rule,0,false,Timer.start());
        }
    }

    public GatewayContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive,
                          GatewayRequest request,Rule rule,int currentRetryTimes,boolean gray,Timer.Sample timerSample){
        super(protocol, nettyCtx, keepAlive);
        this.request = request;
        this.rule = rule;
        this.currentRetryTimes = currentRetryTimes;
        this.gray = gray;
        this.timerSample = timerSample;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Rule.FilterConfig getFilterConfig(String filterId){
        return  rule.getFilterConfig(filterId);
    }

    public String getUniqueId(){
        return request.getUniqueId();
    }

    public void releaseRequest(){
        if(requestReleased.compareAndSet(false,true)){
            ReferenceCountUtil.release(request.getFullHttpRequest());
        }
    }

    public GatewayRequest getOriginRequest(){
        return  request;
    }
}


package com.lls.core.filter.flowCtl.policies;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.StringUtils;
import com.lls.common.config.Rule;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * 单机限流
 */
public class SingleNodeRateLimiter {

    private RateLimiter rateLimiter;
    private  double maxPermits;

    public SingleNodeRateLimiter(double maxPermits) {
        this.maxPermits = maxPermits;
        rateLimiter = RateLimiter.create(maxPermits);
    }

    public SingleNodeRateLimiter(double maxPermits, long warmUpPeriodAsSecond) {
        this.maxPermits = maxPermits;
        rateLimiter = RateLimiter.create(maxPermits,warmUpPeriodAsSecond, TimeUnit.SECONDS);
    }

    public static ConcurrentHashMap<String, SingleNodeRateLimiter> resourceRateLimiterMap = new ConcurrentHashMap<String, SingleNodeRateLimiter>();

    public static SingleNodeRateLimiter getInstance(String serviceId , Rule.FlowCtlConfig flowCtlConfig,int maxPermits){
        if(StringUtils.isEmpty(serviceId) || flowCtlConfig ==null ||
                StringUtils.isEmpty(flowCtlConfig.getValue()) ||
                StringUtils.isEmpty(flowCtlConfig.getConfig()) ||
                StringUtils.isEmpty(flowCtlConfig.getType())){
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        String key = buffer.append(serviceId).append(".").append(flowCtlConfig.getValue()).toString();
        SingleNodeRateLimiter countLimiter = resourceRateLimiterMap.get(key);
        if(countLimiter == null){
            countLimiter = new SingleNodeRateLimiter(maxPermits);
            resourceRateLimiterMap.putIfAbsent(key,countLimiter);
        }
        return countLimiter;
    }

    public boolean acquire(int permits){
        boolean success  = rateLimiter.tryAcquire(permits);
        if(success){
            return true;
        }
      return  false;
    }
}

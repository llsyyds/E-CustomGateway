package com.lls.core.filter.flowCtl;

import com.alibaba.fastjson.JSON;
import com.lls.common.constants.FilterConst;
import com.lls.core.redis.JedisUtil;
import org.apache.commons.lang3.StringUtils;
import com.lls.common.config.Rule;
import com.lls.core.filter.flowCtl.policies.DistributedRateLimiter;
import com.lls.core.filter.flowCtl.policies.SingleNodeRateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据路基进行流控
 */
public class FlowCtlByPathRule implements IGatewayFlowCtlRule{

    private String serviceId;

    private String path;

    private DistributedRateLimiter redisCountLimiter;

    private static final String LIMIT_MESSAGE ="您的请求过于频繁,请稍后重试";

    public FlowCtlByPathRule(String serviceId, String path, DistributedRateLimiter redisCountLimiter) {
        this.serviceId = serviceId;
        this.path = path;
        this.redisCountLimiter = redisCountLimiter;
    }

    private static ConcurrentHashMap<String,FlowCtlByPathRule> servicePathMap = new ConcurrentHashMap<>();

    public static  FlowCtlByPathRule getInstance(String serviceId, String path){
        StringBuffer buffer = new StringBuffer();
        String key = buffer.append(serviceId).append(".").append(path).toString();
        FlowCtlByPathRule flowCtlByPathRule = servicePathMap.get(key);
        if(flowCtlByPathRule == null){
            flowCtlByPathRule = new FlowCtlByPathRule(serviceId,path,new DistributedRateLimiter(new JedisUtil()));
            servicePathMap.put(key,flowCtlByPathRule);
        }
        return  flowCtlByPathRule;
    }

    /**
     * 根据路径执行流控
     * @param flowCtlConfig
     * @param serviceId
     */
    @Override
    public void doFlowCtlFilter(Rule.FlowCtlConfig flowCtlConfig, String serviceId) {
        if(flowCtlConfig == null || StringUtils.isEmpty(serviceId) || StringUtils.isEmpty(flowCtlConfig.getConfig())){
            return;
        }
        Map<String,Integer> configMap = JSON.parseObject(flowCtlConfig.getConfig(),Map.class);
        if((!configMap.containsKey(FilterConst.FLOW_CTL_LIMIT_DURATION) || !configMap.containsKey(FilterConst.FLOW_CTL_LIMIT_PERMITS )) && !configMap.containsKey(FilterConst.FLOW_CTL_LIMIT_MAX_PERMITS)){
            return;
        }
        StringBuffer buffer = new StringBuffer();
        boolean flag = true;
        String key = buffer.append(serviceId).append(".").append(path).toString();
        if(FilterConst.FLOW_CTL_MODEL_DISTRIBUTED.equalsIgnoreCase(flowCtlConfig.getModel())){
            double duration = configMap.get(FilterConst.FLOW_CTL_LIMIT_DURATION);
            double permits = configMap.get(FilterConst.FLOW_CTL_LIMIT_PERMITS);
            flag = redisCountLimiter.doFlowCtl(key,(int)permits,(int)duration);
        }else {
            int maxPermits = configMap.get(FilterConst.FLOW_CTL_LIMIT_MAX_PERMITS);
            System.out.println(maxPermits);
            SingleNodeRateLimiter guavaCountLimiter = SingleNodeRateLimiter.getInstance(serviceId,flowCtlConfig,maxPermits);
            if(guavaCountLimiter == null){
                throw  new RuntimeException("获取单机限流工具类为空");
            }
            flag = guavaCountLimiter.acquire(1);
        }
        if(!flag){
            throw new RuntimeException(LIMIT_MESSAGE);
        }
    }
}

package com.lls.core.filter.loadbalance;

import com.lls.common.config.DynamicConfigManager;
import com.lls.common.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import com.lls.common.config.ServiceInstance;
import com.lls.core.context.GatewayContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lls.common.enums.ResponseCode.SERVICE_INSTANCE_NOT_FOUND;

/**
 * 负载均衡-轮询
 */
@Slf4j
public class RoundRobinLoadBalanceRule implements  IGatewayLoadBalanceRule{

    private AtomicInteger position = new AtomicInteger(1);

    private final  String serviceId;


    public RoundRobinLoadBalanceRule( String serviceId) {
        this.serviceId = serviceId;
    }

    private static ConcurrentHashMap<String,RoundRobinLoadBalanceRule> serviceMap = new ConcurrentHashMap<>();

    public static RoundRobinLoadBalanceRule getInstance(String serviceId){
        RoundRobinLoadBalanceRule loadBalanceRule = serviceMap.get(serviceId);
        if(loadBalanceRule == null){
            loadBalanceRule = new RoundRobinLoadBalanceRule(serviceId);
            serviceMap.put(serviceId,loadBalanceRule);
        }
        return loadBalanceRule;
    }

    @Override
    public ServiceInstance choose(GatewayContext ctx) {
        String serviceId = ctx.getUniqueId();
        boolean gray = ctx.isGray();
        return choose(serviceId,gray);
    }

    @Override
    public ServiceInstance choose(String serviceId,boolean gray) {
        Set<ServiceInstance> serviceInstanceSet =  DynamicConfigManager.getInstance().getServiceInstanceByUniqueId(serviceId,gray);
        if(serviceInstanceSet.isEmpty()){
            log.warn("No instance available for:{}",serviceId);
            throw  new NotFoundException(SERVICE_INSTANCE_NOT_FOUND);
        }
        List<ServiceInstance> instances = new ArrayList<ServiceInstance>(serviceInstanceSet);
        if(instances.isEmpty()){
            log.warn("No instance available for service:{}",serviceId);
            return null;
        }else{
            int pos = Math.abs(this.position.incrementAndGet());
            return instances.get(pos%instances.size());
        }
    }
}

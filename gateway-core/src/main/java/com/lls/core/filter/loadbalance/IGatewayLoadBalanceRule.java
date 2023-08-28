package com.lls.core.filter.loadbalance;

import com.lls.common.config.ServiceInstance;
import com.lls.core.context.GatewayContext;

/**
 * 负载均衡顶级接口
 */
public interface IGatewayLoadBalanceRule {

    /**
     * 通过上下文参数获取服务实例
     * @param ctx
     * @return
     */
    ServiceInstance choose(GatewayContext ctx);

    /**
     * 通过服务ID拿到对应的服务实例
     * @param serviceId
     * @return
     */
    ServiceInstance choose(String serviceId,boolean gray);
}

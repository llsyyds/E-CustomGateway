package com.lls.gateway.register.center.api;

import com.lls.common.config.ServiceDefinition;
import com.lls.common.config.ServiceInstance;

public interface RegisterCenter {

    /**
     * 初始化
     * @param registerAddress
     * @param namespace
     * @param group
     */
    void init(String registerAddress,String namespace, String group);

    /**
     * 注册·
     * @param serviceDefinition
     * @param serviceInstance
     */
    void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance);

    /**
     * 注销
     * @param serviceDefinition
     * @param serviceInstance
     */
    void deregister(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance);

    /**
     * 订阅所有服务变更
     * @param registerCenterListener
     */
    void subscribeAllServices(RegisterCenterListener registerCenterListener);
}

package com.lls.gateway.register.center.api;

import com.lls.common.config.ServiceDefinition;
import com.lls.common.config.ServiceInstance;

import java.util.Set;

public interface RegisterCenterListener {

    void onChange(ServiceDefinition serviceDefinition,
                  Set<ServiceInstance> serviceInstanceSet);
}

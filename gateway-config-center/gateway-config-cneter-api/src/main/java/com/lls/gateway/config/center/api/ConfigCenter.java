package com.lls.gateway.config.center.api;

public interface ConfigCenter {

    void init(String serverAddr,String namespace ,String env);

    void subscribeRulesChange(RulesChangeListener listener);
}

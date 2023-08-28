package com.lls.gateway.config.center.nacos;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.lls.common.config.Rule;
import lombok.extern.slf4j.Slf4j;
import com.lls.gateway.config.center.api.ConfigCenter;
import com.lls.gateway.config.center.api.RulesChangeListener;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

@Slf4j
public class NacosConfigCenter implements ConfigCenter {
    private static final String DATA_ID = "api-gateway";

    private String serverAddr;

    private String namespace;

    private String group;

    private ConfigService configService;

    @Override
    public void init(String serverAddr,String namespace ,String group) {
        this.serverAddr = serverAddr;
        this.namespace = namespace;
        this.group = group;

        Properties properties = new Properties();
        //设置注册中心地址
        properties.setProperty(PropertyKeyConst.SERVER_ADDR,serverAddr);
        //设置命名空间
        properties.setProperty(PropertyKeyConst.NAMESPACE,namespace);

        try {
            configService = NacosFactory.createConfigService(properties);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribeRulesChange(RulesChangeListener listener) {
        try {
            //初始化通知
            String config = configService.getConfig(DATA_ID, group, 5000);
            //{"rules":[{}, {}]}
            log.info("config from nacos: {}", config);
            List<Rule> rules = JSON.parseObject(config).getJSONArray("rules").toJavaList(Rule.class);
            listener.onRulesChange(rules);

            //监听变化
            configService.addListener(DATA_ID, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("config from nacos: {}", configInfo);
                    List<Rule> rules = JSON.parseObject(configInfo).getJSONArray("rules").toJavaList(Rule.class);
                    listener.onRulesChange(rules);
                }
            });

        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }
}

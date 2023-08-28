package com.lls.core.filter.loadbalance;

import com.alibaba.fastjson.JSON;
import com.lls.common.constants.BasicConst;
import com.lls.common.constants.FilterConst;
import com.lls.common.exception.NotFoundException;
import com.lls.core.filter.Filter;
import com.lls.core.filter.FilterAspect;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.lls.common.config.Rule;
import com.lls.common.config.ServiceInstance;
import com.lls.core.context.GatewayContext;
import com.lls.core.request.GatewayRequest;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.lls.common.enums.ResponseCode.SERVICE_INSTANCE_NOT_FOUND;

/**
 * 负载均衡过滤器
 */
@Slf4j
@FilterAspect(id= FilterConst.LOAD_BALANCE_FILTER_ID,
              name = FilterConst.LOAD_BALANCE_FILTER_NAME,
              order = FilterConst.LOAD_BALANCE_FILTER_ORDER)
public class LoadBalanceFilter implements Filter {

    @Override
    public void doFilter(GatewayContext ctx){
        String serviceId = ctx.getUniqueId();
        boolean gray = ctx.isGray();
        IGatewayLoadBalanceRule gatewayLoadBalanceRule = getLoadBalanceRule(ctx);
        ServiceInstance serviceInstance = gatewayLoadBalanceRule.choose(serviceId,gray);
        System.out.println("IP为"+serviceInstance.getIp()+",端口号："+serviceInstance.getPort());
        GatewayRequest request = ctx.getRequest();
        if(serviceInstance != null && request != null){
            String host  = serviceInstance.getIp()+":"+serviceInstance.getPort();
            request.setModifyHost(host);
        }else{
            log.warn("No instance available for :{}",serviceId);
            throw new NotFoundException(SERVICE_INSTANCE_NOT_FOUND);
        }
    }


    /**
     * 根据配置获取负载均衡器
     * @param ctx
     * @return
     */
    public IGatewayLoadBalanceRule getLoadBalanceRule(GatewayContext ctx) {
        IGatewayLoadBalanceRule loadBalanceRule = null;
        Rule configRule = ctx.getRule();
        if (configRule != null) {
            Set<Rule.FilterConfig> filterConfigs = configRule.getFilterConfigs();
            Iterator iterator = filterConfigs.iterator();
            Rule.FilterConfig filterConfig;
            while (iterator.hasNext()) {
                filterConfig = (Rule.FilterConfig) iterator.next();
                if (filterConfig == null) {
                    continue;
                }
                String filterId = filterConfig.getId();
                if (filterId.equals(FilterConst.LOAD_BALANCE_FILTER_ID)) {
                    String config = filterConfig.getConfig();
                    String strategy = FilterConst.LOAD_BALANCE_STRATEGY_RANDOM;
                    if (StringUtils.isNotEmpty(config)) {
                        Map<String, String> mapTypeMap = JSON.parseObject(config, Map.class);
                        strategy = mapTypeMap.getOrDefault(FilterConst.LOAD_BALANCE_KEY, strategy);
                    }
                    switch (strategy) {
                        case FilterConst.LOAD_BALANCE_STRATEGY_RANDOM:
                            loadBalanceRule = RandomLoadBalanceRule.getInstance(configRule.getServiceId());
                            break;
                        case FilterConst.LOAD_BALANCE_STRATEGY_ROUND_ROBIN:
                            loadBalanceRule = RoundRobinLoadBalanceRule.getInstance(configRule.getServiceId());
                            break;
                        default:
                            log.warn("No loadBalance strategy for service:{}", strategy);
                            loadBalanceRule = RandomLoadBalanceRule.getInstance(configRule.getServiceId());
                            break;
                    }
                }
            }
        }
        return loadBalanceRule;
    }
}

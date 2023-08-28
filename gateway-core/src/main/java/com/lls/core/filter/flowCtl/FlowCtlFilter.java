package com.lls.core.filter.flowCtl;

import com.lls.common.constants.FilterConst;
import com.lls.core.filter.Filter;
import com.lls.core.filter.FilterAspect;
import lombok.extern.slf4j.Slf4j;
import com.lls.common.config.Rule;
import com.lls.core.context.GatewayContext;

import java.util.Iterator;
import java.util.Set;

/**
 * 限流流控过滤器
 */
@Slf4j
@FilterAspect(id= FilterConst.FLOW_CTL_FILTER_ID,
        name = FilterConst.FLOW_CTL_FILTER_NAME,
        order = FilterConst.FLOW_CTL_FILTER_ORDER)
public class FlowCtlFilter implements Filter {
    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        Rule rule = ctx.getRule();
        if(rule != null){
            Set<Rule.FlowCtlConfig>  flowCtlConfigs = rule.getFlowCtlConfigs();
            Iterator iterator = flowCtlConfigs.iterator();
            Rule.FlowCtlConfig flowCtlConfig;
            while (iterator.hasNext()){
                IGatewayFlowCtlRule flowCtlRule = null;
                flowCtlConfig = (Rule.FlowCtlConfig)iterator.next();
                if(flowCtlConfig == null){
                    continue;
                }
                String path = ctx.getRequest().getPath();
                if(flowCtlConfig.getType().equalsIgnoreCase(FilterConst.FLOW_CTL_TYPE_PATH)
                && path.equals(flowCtlConfig.getValue())){
                    flowCtlRule = FlowCtlByPathRule.getInstance(rule.getServiceId(),path);
                }else if(flowCtlConfig.getType().equalsIgnoreCase(FilterConst.FLOW_CTL_TYPE_SERVICE)){

                }
                if(flowCtlRule != null){
                    flowCtlRule.doFlowCtlFilter(flowCtlConfig,rule.getServiceId());
                }
            }
        }
    }
}

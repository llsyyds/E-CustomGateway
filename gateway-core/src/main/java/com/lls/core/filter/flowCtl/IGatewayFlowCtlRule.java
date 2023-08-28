package com.lls.core.filter.flowCtl;

import com.lls.common.config.Rule;

/**
 * 执行限流的接口
 */
public interface IGatewayFlowCtlRule {

    void doFlowCtlFilter(Rule.FlowCtlConfig flowCtlConfig,String serviceId);

}

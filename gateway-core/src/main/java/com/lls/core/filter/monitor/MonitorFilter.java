package com.lls.core.filter.monitor;

import com.lls.core.context.GatewayContext;
import com.lls.core.filter.Filter;
import com.lls.core.filter.FilterAspect;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import static com.lls.common.constants.FilterConst.*;

@Slf4j
@FilterAspect(id=MONITOR_FILTER_ID,
        name = MONITOR_FILTER_NAME,
        order = MONITOR_FILTER_ORDER)
public class MonitorFilter implements Filter {
    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        //开始计时，并将计时器设置到ctx的timeSample字段中
        ctx.setTimerSample(Timer.start());
    }
}

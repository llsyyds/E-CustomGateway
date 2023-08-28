package com.lls.core.filter.gray;

import com.lls.core.context.GatewayContext;
import com.lls.core.filter.Filter;
import com.lls.core.filter.FilterAspect;
import lombok.extern.slf4j.Slf4j;

import static com.lls.common.constants.FilterConst.*;

/**
 * @PROJECT_NAME: api-gateway
 * @DESCRIPTION: 灰度发布过滤器
 * @AUTHOR: lls
 * @DATE: 2023/7/24 16:35
 */

@Slf4j
@FilterAspect(id=GRAY_FILTER_ID,
        name = GRAY_FILTER_NAME,
        order = GRAY_FILTER_ORDER)
public class GrayFilter implements Filter {
    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        //测试灰度功能待时候使用
        String gray = ctx.getRequest().getHeaders().get("gray");
        if ("true".equals(gray)) {
            ctx.setGray(true);
        }

        //如果请求头里面没有gray:true的请求头（即显示设置）
        //就通过下面判断即在所有的请求中，有大约1/1024的概率会被标记为灰度请求
        String clientIp = ctx.getRequest().getClientIp();
        int res = clientIp.hashCode() & (1024 - 1); //等价于对1024取模
        if (res == 1) {
            //1024分之一对概率
            ctx.setGray(true);
        }

    }
}

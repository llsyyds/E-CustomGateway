package com.lls.core.filter.user;

import com.lls.common.enums.ResponseCode;
import com.lls.common.exception.ResponseException;
import com.lls.core.context.GatewayContext;
import com.lls.core.filter.Filter;
import com.lls.core.filter.FilterAspect;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static com.lls.common.constants.FilterConst.*;

/**
 * @PROJECT_NAME: api-gateway
 * @DESCRIPTION: 用户验证过滤器
 * 负责处理并转发请求，然后处理请求的结果
 * @USER: WuYang
 * @DATE: 2023/3/12 22:22
 */

@Slf4j
@FilterAspect(id= USER_AUTH_FILTER_ID,
        name = USER_AUTH_FILTER_NAME,
        order =USER_AUTH_FILTER_ORDER )
public class UserAuthFilter implements Filter {
    private static final String SECRET_KEY = "faewifheafewhefsfjkds";
    private static final String COOKIE_NAME = "user-jwt";

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        //检查是否需要用户鉴权
        if (ctx.getRule().getFilterConfig(USER_AUTH_FILTER_ID) == null) {
            return;
        }

        String token = ctx.getRequest().getCookie(COOKIE_NAME).value();
        if (StringUtils.isBlank(token)) {
            throw new ResponseException(ResponseCode.UNAUTHORIZED);
        }

        try {
            //解析用户id
            long userId = parseUserId(token);
            //把用户id传给下游
            ctx.getRequest().setUserId(userId);
        } catch (Exception e) {
            throw new ResponseException(ResponseCode.UNAUTHORIZED);
        }

    }

    private long parseUserId(String token) {
        Jwt jwt = Jwts.parser().setSigningKey(SECRET_KEY).parse(token);
        return Long.parseLong(((DefaultClaims)jwt.getBody()).getSubject());
    }
}

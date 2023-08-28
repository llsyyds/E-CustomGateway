package com.lls.common.enums;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;

@Getter
public enum ResponseCode {
    //2000系列表示成功、4000系列表示客户端错误，如请求参数错误、授权错误等、
    // 5000系列表示服务器端错误，如内部错误、配置错误等、4040系列表示找不到资源的相关错误、
    // 4030系列表示访问被禁止的错误。

    // 1. 基本状态
    SUCCESS(HttpResponseStatus.OK, 2000, "成功"),
    INTERNAL_ERROR(HttpResponseStatus.INTERNAL_SERVER_ERROR, 5000, "网关内部错误"),
    SERVICE_UNAVAILABLE(HttpResponseStatus.SERVICE_UNAVAILABLE, 5030, "服务暂时不可用,请稍后再试"),

    // 2. 请求错误
    REQUEST_PARSE_ERROR(HttpResponseStatus.BAD_REQUEST, 4002, "请求解析错误, header中必须存在uniqueId参数"),
    REQUEST_PARSE_ERROR_NO_UNIQUEID(HttpResponseStatus.BAD_REQUEST, 4003, "请求解析错误, header中必须存在uniqueId参数"),
    VERIFICATION_FAILED(HttpResponseStatus.BAD_REQUEST, 4006, "请求参数校验失败"),
    REQUEST_TIMEOUT(HttpResponseStatus.GATEWAY_TIMEOUT, 5040, "连接下游服务超时"),
    HTTP_RESPONSE_ERROR(HttpResponseStatus.INTERNAL_SERVER_ERROR, 5002, "服务返回异常"),

    // 3. 资源查找错误
    PATH_NO_MATCHED(HttpResponseStatus.NOT_FOUND, 4040, "没有找到匹配的路径, 请求快速失败"),
    SERVICE_DEFINITION_NOT_FOUND(HttpResponseStatus.NOT_FOUND, 4041, "未找到对应的服务定义"),
    SERVICE_INVOKER_NOT_FOUND(HttpResponseStatus.NOT_FOUND, 4042, "未找到对应的调用实例"),
    SERVICE_INSTANCE_NOT_FOUND(HttpResponseStatus.NOT_FOUND, 4043, "未找到对应的服务实例"),
    DUBBO_METHOD_NOT_FOUNT(HttpResponseStatus.NOT_FOUND, 4044, "方法不存在"),

    // 4. Dubbo相关错误
    DUBBO_DISPATCH_CONFIG_EMPTY(HttpResponseStatus.INTERNAL_SERVER_ERROR, 5003, "路由配置不能为空"),
    DUBBO_PARAMETER_TYPE_EMPTY(HttpResponseStatus.BAD_REQUEST, 4004, "请求的参数类型不能为空"),
    DUBBO_PARAMETER_VALUE_ERROR(HttpResponseStatus.BAD_REQUEST, 4005, "请求参数解析错误"),
    DUBBO_CONNECT_ERROR(HttpResponseStatus.INTERNAL_SERVER_ERROR, 5004, "下游服务发生异常,请稍后再试"),
    DUBBO_REQUEST_ERROR(HttpResponseStatus.INTERNAL_SERVER_ERROR, 5005, "服务请求异常"),
    DUBBO_RESPONSE_ERROR(HttpResponseStatus.INTERNAL_SERVER_ERROR, 5006, "服务返回异常"),

    // 5. 权限错误
    UNAUTHORIZED(HttpResponseStatus.UNAUTHORIZED, 4001, "用户未登陆"),
    BLACKLIST(HttpResponseStatus.FORBIDDEN, 4030, "请求IP在黑名单"),
    WHITELIST(HttpResponseStatus.FORBIDDEN, 4031, "请求IP不在白名单"),

    // 其他错误
    FILTER_CONFIG_PARSE_ERROR(HttpResponseStatus.INTERNAL_SERVER_ERROR, 5001, "过滤器配置解析异常")
    ;

    private HttpResponseStatus httpResponseStatus;
    private int appCode;
    private String message;

    ResponseCode(HttpResponseStatus httpResponseStatus, int appCode, String msg) {
        this.httpResponseStatus = httpResponseStatus;
        this.appCode = appCode;
        this.message = msg;
    }
}

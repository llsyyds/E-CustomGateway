package com.lls.core.response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lls.common.utils.JSONUtil;
import io.netty.handler.codec.http.*;
import lombok.Data;
import org.asynchttpclient.Response;
import com.lls.common.enums.ResponseCode;

import java.util.List;
import java.util.Map;

/**
 * @PROJECT_NAME: api-gateway
 * @DESCRIPTION:
 * @USER: WuYang
 * @DATE: 2022/12/29 21:01
 */
@Data
public class GatewayResponse {

    /**
     * 响应头
     */
    private HttpHeaders responseHeaders = new DefaultHttpHeaders();

    /**
     * 额外的响应结果
     */
    private final HttpHeaders extraResponseHeaders = new DefaultHttpHeaders();
    /**
     * 响应内容
     */
    private String content;

    /**
     * 异步返回对象
     */
    private Response futureResponse;

    /**
     * 响应返回码
     */
    private HttpResponseStatus httpResponseStatus;


    public GatewayResponse(){

    }

    /**
     * 设置响应头信息
     * @param key
     * @param val
     */
    public  void  putHeader(CharSequence key, CharSequence val){
        responseHeaders.add(key,val);
    }

    /**
     * 构建异步响应对象
     * @param futureResponse
     * @return
     */
    public  static  GatewayResponse buildGatewayResponse(Response futureResponse){
        GatewayResponse response = new GatewayResponse();
        response.setFutureResponse(futureResponse);
        response.setHttpResponseStatus(HttpResponseStatus.valueOf(futureResponse.getStatusCode()));
       return response;
    }

    /**
     * 构建成功响应对象
     * @param data
     * @return
     */
    public  static GatewayResponse buildSuccessGatewayResponse(Object data){
        ObjectNode objectNode = JSONUtil.createObjectNode();
        GatewayResponse gatewayResponse = new GatewayResponse();

        if (data instanceof Response){
            Response response = (Response) data;
            List<Map.Entry<String, String>> heads = response.getHeaders().entries();
            heads.stream().forEach(entry -> {
                String headName = entry.getKey();
                String headValue = entry.getValue();
                gatewayResponse.putHeader(headName, headValue);
            });
            objectNode.putPOJO("data",response.getResponseBody());
        }else {
            objectNode.putPOJO("data", data);
        }
        gatewayResponse.setHttpResponseStatus(ResponseCode.SUCCESS.getHttpResponseStatus());

        gatewayResponse.putHeader(HttpHeaderNames.CONTENT_TYPE,HttpHeaderValues.APPLICATION_JSON+";charset=utf-8");

        gatewayResponse.setContent(JSONUtil.toJSONString(objectNode));

        return  gatewayResponse;
    }

    /**
     * 构建失败响应对象
     * @param responseCode
     * @return
     */
    public  static GatewayResponse buildFailureGatewayResponse(ResponseCode responseCode){
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put("appCode", responseCode.getAppCode());
        objectNode.put("message",responseCode.getMessage());

        GatewayResponse gatewayResponse = new GatewayResponse();
        gatewayResponse.setHttpResponseStatus(responseCode.getHttpResponseStatus());
        gatewayResponse.putHeader(HttpHeaderNames.CONTENT_TYPE,HttpHeaderValues.APPLICATION_JSON+";charset=utf-8");
        gatewayResponse.setContent(JSONUtil.toJSONString(objectNode));
        return gatewayResponse;
    }

}

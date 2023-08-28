package com.lls.core.helper;

import com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.CharsetUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lls.common.constants.BasicConst;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import com.lls.common.enums.ResponseCode;
import com.lls.core.context.GatewayContext;
import com.lls.core.response.GatewayResponse;

/**
 * 响应的辅助类
 */
public class ResponseHelper {

	/**
	 * 获取请求在过滤器处理阶段的异常响应
	 * @param responseCode
	 * @return
	 */
	public static FullHttpResponse getProcessFailHttpResponse(ResponseCode responseCode) {
		GatewayResponse gatewayResponse = GatewayResponse.buildFailureGatewayResponse(responseCode);
		DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, 
				responseCode.getHttpResponseStatus(),
				Unpooled.wrappedBuffer(gatewayResponse.getContent().getBytes()));
		
		httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
		httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
		return httpResponse;
	}

	/**
	 * 获取请求在具体后端反馈的成功/异常响应
	 * @param gatewayResponse
	 * @return
	 */
	private static FullHttpResponse getHttpResponse(GatewayResponse gatewayResponse) {
		ByteBuf content;
		if(gatewayResponse.getContent() != null) {
			content = Unpooled.wrappedBuffer(gatewayResponse.getContent().getBytes(CharsetUtil.UTF_8));
			//判断gatewayResponse是否为成功请求
			if (gatewayResponse.getHttpResponseStatus().code() == 200){
				String contentStrig = content.toString(CharsetUtil.UTF_8);
				ObjectMapper mapper = new ObjectMapper();
				try {
					JsonNode jsonNode = mapper.readTree(contentStrig);
					//提起context里面的"data"对应的值
					String dataValue = jsonNode.path("data").asText();
					content = Unpooled.copiedBuffer(dataValue,CharsetUtil.UTF_8);
				}catch (Exception e){
					throw new RuntimeException("Failed to pare JSON",e);
				}
			}
		} else {
			content = Unpooled.wrappedBuffer(BasicConst.BLANK_SEPARATOR_1.getBytes());
		}

		DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1,
				gatewayResponse.getHttpResponseStatus(),
				content);
		httpResponse.headers().add(gatewayResponse.getResponseHeaders());
		httpResponse.headers().add(gatewayResponse.getExtraResponseHeaders());
		httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
		return httpResponse;
	}

	/**
	 * 写回响应信息方法
	 */
	public static void writeResponse(GatewayContext context) {
		if(context.isWritten()) {
			//构建响应对象
			FullHttpResponse httpResponse = ResponseHelper.getHttpResponse((GatewayResponse)context.getResponse());

			if(!context.isKeepAlive()) {
				context.getNettyCtx().writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
			}
			else {
				httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
				context.getNettyCtx().writeAndFlush(httpResponse);
			}

			context.completed();
		} else if(context.isCompleted()){
			context.invokeCompletedCallBack();
		}
		
	}
	
}

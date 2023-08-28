package com.lls.core.netty.processor;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import com.lls.common.enums.ResponseCode;
import com.lls.common.exception.BaseException;
import com.lls.core.context.GatewayContext;
import com.lls.core.context.HttpRequestWrapper;
import com.lls.core.filter.FilterFactory;
import com.lls.core.filter.GatewayFilterChainFactory;
import com.lls.core.helper.RequestHelper;
import com.lls.core.helper.ResponseHelper;


@Slf4j
public class NettyCoreProcessor implements NettyProcessor {

    private FilterFactory filterFactory = GatewayFilterChainFactory.getInstance();

    @Override
    public void process(HttpRequestWrapper wrapper) {
        FullHttpRequest fullHttpRequest = wrapper.getFullHttpRequest();
        ChannelHandlerContext ctx = wrapper.getCtx();
        GatewayContext gatewayContext = null;
        try {
            gatewayContext = RequestHelper.doContext(fullHttpRequest, ctx);
            //执行过滤器逻辑
            filterFactory.buildFilterChain(gatewayContext).doFilter(gatewayContext);

          //下面的异常是指请求还没到达具体后端服务就报了，所以就直接返回给客户端了
        } catch (BaseException e) {
            log.error("process error {} {}", e.getCode().getAppCode(), e.getCode().getMessage());
            FullHttpResponse fullHttpResponse = ResponseHelper.getProcessFailHttpResponse(e.getCode());
            doWriteAndRelease(gatewayContext,ctx, fullHttpRequest, fullHttpResponse);
        } catch (Throwable t) {
            log.error("process unkown error", t);
            FullHttpResponse httpResponse = ResponseHelper.getProcessFailHttpResponse(ResponseCode.INTERNAL_ERROR);
            doWriteAndRelease(gatewayContext,ctx, fullHttpRequest, httpResponse);
        }

    }

    //该方法的作用是将响应(httpResponse)写回给客户端并随后释放相关资源
    private void doWriteAndRelease(GatewayContext gatewayContext,ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest, FullHttpResponse httpResponse) {
        gatewayContext.written();
        ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
        //这里这样释放比较保险（怕gatewayContext里的fullRequest还没设置进行就报异常了，那时候调用gatewayContext.releaseRequest可能会出问题）
        ReferenceCountUtil.release(fullHttpRequest);
        gatewayContext.completed();
    }

    @Override
    public void start() {

    }

    @Override
    public void shutDown() {

    }
}

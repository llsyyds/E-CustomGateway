package com.lls.core.filter.router;

import com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.CharsetUtil;
import com.lls.common.constants.FilterConst;
import com.lls.common.exception.ConnectException;
import com.lls.common.exception.ResponseException;
import com.lls.core.ConfigLoader;
import com.lls.core.filter.Filter;
import com.lls.core.filter.FilterAspect;
import com.lls.core.helper.ResponseHelper;
import com.netflix.hystrix.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import com.lls.common.config.Rule;
import com.lls.common.enums.ResponseCode;
import com.lls.core.context.GatewayContext;
import com.lls.core.helper.AsyncHttpHelper;
import com.lls.core.response.GatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * 路由过滤器
 */
@Slf4j
@FilterAspect(id= FilterConst.ROUTER_FILTER_ID,
              name = FilterConst.ROUTER_FILTER_NAME,
              order = FilterConst.ROUTER_FILTER_ORDER)
public class RouterFilter implements Filter {
    //定义了一个名为accessLog的日志记录器
    private static Logger accessLog = LoggerFactory.getLogger("accessLog");
    //标记
    private boolean flag = false;
    //这里的标记只是为了测试用的，上线的时候得去掉
    private boolean flag2 = false;

    //发送请求到具体后端的路由
    @Override
    public void doFilter(GatewayContext gatewayContext) throws Exception {
        Optional<Rule.HystrixConfig> optMatHystrixConfig = getHystrixConfig(gatewayContext);
        if(optMatHystrixConfig.isPresent() && optMatHystrixConfig.get().isGlobalEnable()){
            routeWithHystrix(gatewayContext,optMatHystrixConfig);
        }else{
            route(gatewayContext,optMatHystrixConfig);
        }
    }

    private static Optional<Rule.HystrixConfig> getHystrixConfig(GatewayContext gatewayContext){
        Rule rule = gatewayContext.getRule();
        Optional<Rule.HystrixConfig> optMatHystrixConfig = rule.getHystrixConfigs().stream()
                .filter(c-> StringUtils.equals(c.getPath(),gatewayContext.getRequest().getPath()))
                .findFirst();
        return optMatHystrixConfig;
    }

    private  CompletableFuture<Response> route(GatewayContext gatewayContext,Optional<Rule.HystrixConfig> optMatHystrixConfig){
        Request request = gatewayContext.getRequest().build();
        CompletableFuture<Response> future = AsyncHttpHelper.getInstance().executeRequest(request);

        boolean whenComplete = ConfigLoader.getConfig().isWhenComplete();

        //决定采用单异步还是双异步模式
        if (whenComplete) {
            future.whenComplete((response, throwable) -> {
                complete(request, response, throwable, gatewayContext,optMatHystrixConfig);
            });
        } else {
            future.whenCompleteAsync((response, throwable) -> {
                complete(request, response, throwable, gatewayContext,optMatHystrixConfig);
            });
        }
        return future;
    }

    private void routeWithHystrix(GatewayContext gatewayContext, Optional<Rule.HystrixConfig> optMatHystrixConfig){
        HystrixCommand.Setter setter = HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey
                        .Factory
                        .asKey(gatewayContext.getUniqueId()))
                .andCommandKey(HystrixCommandKey.Factory
                        .asKey(gatewayContext.getRequest().getPath()))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                        .withCoreSize(optMatHystrixConfig.get().getThreadCoreSize()))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                        .withExecutionTimeoutInMilliseconds(optMatHystrixConfig.get().getTimeoutInMilliseconds())
                        .withExecutionIsolationThreadInterruptOnTimeout(true)
                        .withExecutionTimeoutEnabled(optMatHystrixConfig.get().isTimeoutEnabled())
                        .withMetricsRollingStatisticalWindowInMilliseconds(optMatHystrixConfig.get().getSlidingWindowDuration()) //统计的滑动窗口范围
                        .withMetricsRollingStatisticalWindowBuckets(optMatHystrixConfig.get().getNumberOfWindowSegments()) //滑动窗口被分割的段数
                        .withCircuitBreakerRequestVolumeThreshold(optMatHystrixConfig.get().getRequestThreshold()) //请求次数
                        .withCircuitBreakerErrorThresholdPercentage(optMatHystrixConfig.get().getFailureRateThreshold()) //失败率
                        .withCircuitBreakerSleepWindowInMilliseconds(optMatHystrixConfig.get().getCircuitBreakerResetTime()) //熔断时间
                        .withCircuitBreakerEnabled(optMatHystrixConfig.get().isCircuitBreakerEnabled()));

        new HystrixCommand<Object>(setter) {
            /**
             * run方法失败了（无论是因为实际的异常、超时还是断路器已经打开的状态），
             * 它就不会返回run方法中的结果，而是调用getFallback方法
             * @return
             * @throws Exception
             */
            @Override
            protected Object run() throws Exception {
                flag2 = true;
                route(gatewayContext,optMatHystrixConfig).get();
                return null;
            }

            @Override
            protected Object getFallback(){
                flag = true;
                gatewayContext.releaseRequest();
                gatewayContext.written();
                String fallbackResponseContent;
                if (flag2){
                    fallbackResponseContent = optMatHystrixConfig.get().getFallbackResponse();
                }else {
                    fallbackResponseContent = "熔断时间2";
                }
                ByteBuf contentByteBuf= Unpooled.copiedBuffer(fallbackResponseContent, CharsetUtil.UTF_8);
                DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        ResponseCode.SUCCESS.getHttpResponseStatus(),
                        contentByteBuf
                );
                gatewayContext.getNettyCtx().writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);;
                gatewayContext.completed();
                return null;
            }
        }.execute();
    }


    private void complete(Request request,
                          Response response,
                          Throwable throwable,
                          GatewayContext gatewayContext,
                          Optional<Rule.HystrixConfig> optMatHystrixConfig) {

        //如果已经触发了getFallback了，就不再执行这段逻辑了，避免出现问题
        if (flag){
            flag = false;
            return;
        }

        //释放了请求上下文中的请求对象
        gatewayContext.releaseRequest();

        //获取已重试的次数
        int currentRetryTimes = gatewayContext.getCurrentRetryTimes();

        //获取配置里面设置的需要重试的次数
        int confRetryTimes = gatewayContext.getRule().getRetryConfig().getTimes();

        if ((throwable instanceof TimeoutException || throwable instanceof IOException)
                && confRetryTimes!=0 && currentRetryTimes<=confRetryTimes && optMatHystrixConfig.isPresent()
                && !optMatHystrixConfig.get().isGlobalEnable()) {
            doRetry(gatewayContext,currentRetryTimes);
            return;
        }

        try {
            if (Objects.nonNull(throwable)) {
                String url = request.getUrl();
                if (throwable instanceof TimeoutException) {
                    log.warn("complete time out {}", url);
                    System.out.println(throwable.getMessage());
                    gatewayContext.setThrowable(new ResponseException(ResponseCode.REQUEST_TIMEOUT));
                    gatewayContext.setResponse(GatewayResponse.buildFailureGatewayResponse(ResponseCode.REQUEST_TIMEOUT));
                } else {
                    gatewayContext.setThrowable(new ConnectException(throwable, gatewayContext.getUniqueId(), url, ResponseCode.HTTP_RESPONSE_ERROR));
                    gatewayContext.setResponse(GatewayResponse.buildFailureGatewayResponse(ResponseCode.HTTP_RESPONSE_ERROR));
                }
            } else {
                gatewayContext.setResponse(GatewayResponse.buildSuccessGatewayResponse(response));
            }
        } catch (Throwable t) {
            gatewayContext.setThrowable(new ResponseException(ResponseCode.INTERNAL_ERROR));
            gatewayContext.setResponse(GatewayResponse.buildFailureGatewayResponse(ResponseCode.INTERNAL_ERROR));
            log.error("complete error", t);
        } finally {
            gatewayContext.written();
            ResponseHelper.writeResponse(gatewayContext);

            accessLog.info("{} {} {} {} {} {} {}",
                    System.currentTimeMillis() - gatewayContext.getRequest().getBeginTime(),
                    gatewayContext.getRequest().getClientIp(),
                    gatewayContext.getRequest().getUniqueId(),
                    gatewayContext.getRequest().getMethod(),
                    gatewayContext.getRequest().getPath(),
                    gatewayContext.getResponse().getHttpResponseStatus().code(),
                    gatewayContext.getResponse().getFutureResponse().getResponseBodyAsBytes().length);


        }
    }

    //进行重试的方法
    private void doRetry(GatewayContext gatewayContext,int retryTimes){
        System.out.println("当前重试次数为"+retryTimes);
        gatewayContext.setCurrentRetryTimes(retryTimes+1);
        try {
            //发送请求到具体后端的路由
            doFilter(gatewayContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

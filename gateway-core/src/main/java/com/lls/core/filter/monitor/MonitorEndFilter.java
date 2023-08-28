package com.lls.core.filter.monitor;

import com.lls.core.ConfigLoader;
import com.lls.core.context.GatewayContext;
import com.lls.core.filter.Filter;
import com.lls.core.filter.FilterAspect;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.lls.common.constants.FilterConst.*;

@Slf4j
@FilterAspect(id=MONITOR_END_FILTER_ID,
        name = MONITOR_END_FILTER_NAME,
        order = MONITOR_END_FILTER_ORDER)
public class MonitorEndFilter implements Filter {
    //普罗米修斯的注册表
    private final PrometheusMeterRegistry prometheusMeterRegistry;

    public MonitorEndFilter() {
        this.prometheusMeterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        try {
            //暴露接口来提供普罗米修斯指标数据拉取
            HttpServer server = HttpServer.create(new InetSocketAddress(ConfigLoader.getConfig().getPrometheusPort()), 0);

            server.createContext("/prometheus", exchange -> {
                //获取指标数据的文本内容
                String scrape = prometheusMeterRegistry.scrape();

                //指标数据返回
                exchange.sendResponseHeaders(200, scrape.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()){
                    os.write(scrape.getBytes());
                }
            });

            new Thread(server::start).start();

        } catch (IOException exception) {
            log.error("prometheus http server start error", exception);
            throw new RuntimeException(exception);
        }
        log.info("prometheus http server start successful, port:{}", ConfigLoader.getConfig().getPrometheusPort());

        //mock 开发/测试用的 生产可以去掉
        Executors.newScheduledThreadPool(1000).scheduleAtFixedRate(() -> {
            Timer.Sample sample = Timer.start();
            try {
                Thread.sleep(RandomUtils.nextInt(0,100));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Timer timer = prometheusMeterRegistry.timer("gateway_request",
                    "uniqueId", "backend-http-server:1.0.0",
                    "protocol", "http",
                    "path", "/http-server/ping" + RandomUtils.nextInt(0,10));
            sample.stop(timer);
        },200, 100, TimeUnit.MILLISECONDS);
    }

    /**
     * 会在每个请求处理过程中被调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        /**
         * 下面可能的度量数据格式：gateway_request{uniqueId="user-service:1.0", protocol="http", path="/api/v1/users"} 0.123
         */
        //创建一个名为 "gateway_request" 的计时器度量即定义度量格式
        Timer timer = prometheusMeterRegistry.timer("gateway_request",
                "uniqueId", ctx.getUniqueId(),
                "protocol", ctx.getProtocol(),
                "path", ctx.getRequest().getPath());
        //从ctx先获得timeSample然后结束计时并且传入度量格式 Prometheus以上述格式自动收集和存储度量。
        ctx.getTimerSample().stop(timer);
    }
}

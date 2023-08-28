package com.lls.core;

import com.lmax.disruptor.*;
import lombok.Data;

@Data
public class Config {
    private int port = 8888;

    //Prometheus监控的端口
    private int prometheusPort = 18000;

    private String applicationName = "api-gateway";

    private String registryAddress = "127.0.0.1:8848";

    //空格代表的就是public的命名空间id
    private String namespace = "";

    private String group = "DEFAULT_GROUP";

    //netty

    private int eventLoopGroupBossNum = 1;

    private int eventLoopGroupWokerNum = Runtime.getRuntime().availableProcessors();

    private int maxContentLength = 64 * 1024 * 1024;

    //默认单异步模式
    private boolean whenComplete = true;

    //	Http Async 参数选项：

    //	连接超时时间
    private int httpConnectTimeout = 30 * 1000;

    //	请求超时时间
    private int httpRequestTimeout = 30 * 1000;

    //	客户端请求重试次数
    private int httpMaxRequestRetry = 2;

    //	客户端请求最大连接数
    private int httpMaxConnections = 10000;

    //	客户端每个地址支持的最大连接数
    private int httpConnectionsPerHost = 8000;

    //	客户端空闲连接超时时间, 默认60秒
    private int httpPooledConnectionIdleTimeout = 60 * 1000;

    //缓冲区类型
    private String bufferType = "parallel22";

    //缓冲区大小
    private int bufferSize = 1024 * 16;

    //处理线程数
    private int processThread = Runtime.getRuntime().availableProcessors();

    //等待策略
    private String waitStrategy ="blocking";

    public WaitStrategy getWaitStrategy(){
        switch (waitStrategy){
            case "blocking":
                return  new BlockingWaitStrategy();
            case "busySpin":
                return  new BusySpinWaitStrategy();
            case "yielding":
                return  new YieldingWaitStrategy();
            case "sleeping":
                return  new SleepingWaitStrategy();
            default:
                return new BlockingWaitStrategy();
        }
    }

    //扩展.......
}

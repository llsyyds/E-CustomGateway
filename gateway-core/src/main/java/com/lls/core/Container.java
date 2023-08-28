package com.lls.core;

import com.lls.core.netty.processor.DisruptorNettyCoreProcessor;
import lombok.extern.slf4j.Slf4j;
import com.lls.core.netty.NettyHttpClient;
import com.lls.core.netty.NettyHttpServer;
import com.lls.core.netty.processor.NettyCoreProcessor;
import com.lls.core.netty.processor.NettyProcessor;

import static com.lls.common.constants.GatewayConst.BUFFER_TYPE_PARALLEL;

@Slf4j
public class Container implements LifeCycle {
    private final Config config;

    private NettyHttpServer nettyHttpServer;

    private NettyHttpClient nettyHttpClient;

    private NettyProcessor nettyProcessor;

    public Container(Config config) {
        this.config = config;
        init();
    }

    @Override
    public void init() {
        NettyCoreProcessor nettyCoreProcessor = new NettyCoreProcessor();

        if(BUFFER_TYPE_PARALLEL.equals(config.getBufferType())){
            this.nettyProcessor = new DisruptorNettyCoreProcessor(config,nettyCoreProcessor);
        }else{
            this. nettyProcessor = nettyCoreProcessor;
        }

        this.nettyHttpServer = new NettyHttpServer(config, nettyProcessor);

        this.nettyHttpClient = new NettyHttpClient(config,
                nettyHttpServer.getEventLoopGroupWoker());
    }

    @Override
    public void start() {
        nettyProcessor.start();
        nettyHttpServer.start();;
        nettyHttpClient.start();
        log.info("api gateway started!");
    }

    @Override
    public void shutdown() {
        nettyProcessor.shutDown();
        nettyHttpServer.shutdown();
        nettyHttpClient.shutdown();
    }
}

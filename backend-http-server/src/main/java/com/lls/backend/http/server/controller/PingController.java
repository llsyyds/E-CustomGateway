package com.lls.backend.http.server.controller;

import com.lls.gateway.client.core.ApiProtocol;
import lombok.extern.slf4j.Slf4j;
import com.lls.gateway.client.core.ApiInvoker;
import com.lls.gateway.client.core.ApiProperties;
import com.lls.gateway.client.core.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@ApiService(serviceId = "backend-http-server", protocol = ApiProtocol.HTTP, patternPath = "/http-server/**")
public class PingController {

    @Autowired
    private ApiProperties apiProperties;

    @ApiInvoker(path = "/http-server/ping")
    @GetMapping("/http-server/ping")
    public String ping() throws InterruptedException {
        log.info("{}", apiProperties);
//        Thread.sleep(31000);
        Thread.sleep(800);
//        throw new RuntimeException();
        return "pong";
    }
}

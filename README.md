# E-CustomGateway
#### 背景
自研企业级API网关 在现代的微服务架构中，网关是至关重要的组件，它不仅起到路由的作用，还需要处理各种复杂的网络问题如用户的权限认证、限流、降级熔断、灰度发布负载均衡等。Spring Cloud Gateway 作为市场上的佼佼者，已经为众多企业提供了稳定和可靠的服务。但是，每个企业和场景都有其独特性，并且Spring Cloud Gateway对于中小型企业来说用起来 “太重” 了。 在这样的环境下，诞生了自研的企业级网关。与 Spring Cloud Gateway 相比，我们的网关更加灵活和轻量，特别优化了在特定场景和负载下的性能。同时，我们也引入了一些创新的功能和策略，以满足企业级应用中的特定需求。
#### 技术栈
Netty + Nacos + Disruptor + grafana + prometheus。考虑到轻量和功能，目前这几个技术栈是足够的，后续如果要继续完善功能点，可以适当加入其他技术栈。这也体现了自研网关的优点——可定制化。
#### 功能（目前）
 + 路由
 + 负载均衡
 + 限流
 + 灰调发布
 + 请求重试
 + 降级熔断
 + 请求的流量监控
 + 用户的权限认证
#### 功能配置
前提说明：该自研网关中，自定义了 @ApiInvoker 和 @ApiService这两个注解。具体如何使用看演示模块代码（backend-user-service或者backend-http-server）。
 + 路由
   ```
   {
    "rules": [
        {
            "id": "1",
            "name": "test-1",
            "protocol": "http",
            "serviceId": "backend-http-server",
            "prefix": "/user",
            "paths": [
                "/http-server/ping",
                "/user/update"
            ],
            "filterConfigs": [
                {
                    "id": "router_filter"
                }
            ]
        }
      ]
    }
   ```
   将打到网关的客户端请求转发到后端的具体服务。
 + 负载均衡
   ```
   {
    "rules": [
        {
            "id": "1",
            "name": "test-1",
            "protocol": "http",
            "serviceId": "backend-http-server",
            "prefix": "/user",
            "paths": [
                "/http-server/ping",
                "/user/update"
            ],
            "filterConfigs": [
                {
                    "id": "load_balance_filter",
                    "config": {
                        "load_balance": "RoundRobin"
                    }
                },
                {
                    "id": "router_filter"
                }
            ]
        }
      ]
    }
   ```
   目前实现了基于轮询和随机的负载均衡。
 + 限流
   ```
   {
    "rules": [
        {
            "id": "1",
            "name": "test-1",
            "protocol": "http",
            "serviceId": "backend-http-server",
            "prefix": "/user",
            "paths": [
                "/http-server/ping",
                "/user/update"
            ],
            "filterConfigs": [
                {
                    "id": "load_balance_filter",
                    "config": {
                        "load_balance": "RoundRobin"
                    }
                },
                {
                    "id": "router_filter"
                },
                {
                    "id": "flow_ctl_filter"
                }
            ],
            "flowCtlConfigs": [
                {
                    "type": "path",
                    "model": "distributed",
                    "value": "/http-server/ping",
                    "config": {
                        "duration": 10,
                        "permits": 20,
                        "maxPermits": 14
                    }
                }
            ]
        }
      ]
    }
   ```
   目前具有基于redis实现的分布式限流器和基于Guava RateLimiter实现的单机限流器。
 + 灰度发布
   ```
   {
    "rules": [
        {
            "id": "1",
            "name": "test-1",
            "protocol": "http",
            "serviceId": "backend-http-server",
            "prefix": "/user",
            "paths": [
                "/http-server/ping",
                "/user/update"
            ],
            "filterConfigs": [
                {
                    "id": "load_balance_filter",
                    "config": {
                        "load_balance": "RoundRobin"
                    }
                },
                {
                    "id": "router_filter"
                },
                {
                    "id": "flow_ctl_filter"
                },
                {
                    "id":"gray_filter"
                }
            ],
            "flowCtlConfigs": [
                {
                    "type": "path",
                    "model": "distributed",
                    "value": "/http-server/ping",
                    "config": {
                        "duration": 10,
                        "permits": 20,
                        "maxPermits": 14
                    }
                }
            ]
        }
      ]
    }
   ```
   哪个模块被定义为灰度发布模块就在该模块的resource的配置文件里写入 api.gray=true。以此对应的，哪个请求要被转发到灰度发布的模块就必须添加额外请求头——gray:true。
 + 请求重试
   ```
   {
    "rules": [
        {
            "id": "1",
            "name": "test-1",
            "protocol": "http",
            "serviceId": "backend-http-server",
            "prefix": "/user",
            "paths": [
                "/http-server/ping",
                "/user/update"
            ],
            "filterConfigs": [
                {
                    "id": "load_balance_filter",
                    "config": {
                        "load_balance": "RoundRobin"
                    }
                },
                {
                    "id": "router_filter"
                },
                {
                    "id": "flow_ctl_filter"
                },
                {
                    "id":"gray_filter"
                }
            ],
            "flowCtlConfigs": [
                {
                    "type": "path",
                    "model": "distributed",
                    "value": "/http-server/ping",
                    "config": {
                        "duration": 10,
                        "permits": 20,
                        "maxPermits": 14
                    }
                }
            ],
            "retryConfig": {
                "times": 5
            }
        }
      ]
    }
   ```
   目前只实现了简单的请求重试机制，后续可以引入一些请求重试框架如Failsafe、Resilience4j等对该功能进一步完善。
 + 降级熔断
   ```
   {
    "rules": [
        {
            "id": "1",
            "name": "test-1",
            "protocol": "http",
            "serviceId": "backend-http-server",
            "prefix": "/user",
            "paths": [
                "/http-server/ping",
                "/user/update"
            ],
            "filterConfigs": [
                {
                    "id": "load_balance_filter",
                    "config": {
                        "load_balance": "RoundRobin"
                    }
                },
                {
                    "id": "flow_ctl_filter"
                },
                {
                    "id": "router_filter"
                },
                {
                    "id": "gray_filter"
                }
            ],
            "flowCtlConfigs": [
                {
                    "type": "path",
                    "model": "distributed",
                    "value": "/http-server/ping",
                    "config": {
                        "duration": 10,
                        "permits": 20,
                        "maxPermits": 14
                    }
                }
            ],
            "retryConfig": {
                "times": 5
            },
            "hystrixConfigs": [
                {
                    "path": "/http-server/ping",
                    "threadCoreSize": 2,
                    "timeoutInMilliseconds": 1000,
                    "timeoutEnabled": true,
                    "slidingWindowDuration": 30000,
                    "numberOfWindowSegments": 10,
                    "requestThreshold": 4,
                    "failureRateThreshold": 50,
                    "circuitBreakerResetTime": 30000,
                    "circuitBreakerEnabled": true,
                    "fallbackResponse": "熔断时间",
                    "globalEnable": false
                }
            ]
        }
      ]
   }
   ```
   这个降级熔断是通过hystrix实现的，这部分功能很强大，感兴趣的可以好好研究并且进一步扩展。
 + 请求的流量监控
   ```
   {
    "rules": [
        {
            "id": "1",
            "name": "test-1",
            "protocol": "http",
            "serviceId": "backend-http-server",
            "prefix": "/user",
            "paths": [
                "/http-server/ping",
                "/user/update"
            ],
            "filterConfigs": [
                {
                    "id": "load_balance_filter",
                    "config": {
                        "load_balance": "RoundRobin"
                    }
                },
                {
                    "id": "flow_ctl_filter"
                },
                {
                    "id": "router_filter"
                },
                {
                    "id": "gray_filter"
                },
                {
                    "id": "monitor_filter"
                },
                {
                    "id": "monitor_end_filter"
                }
            ],
            "flowCtlConfigs": [
                {
                    "type": "path",
                    "model": "distributed",
                    "value": "/http-server/ping",
                    "config": {
                        "duration": 10,
                        "permits": 20,
                        "maxPermits": 14
                    }
                }
            ],
            "retryConfig": {
                "times": 5
            },
            "hystrixConfigs": [
                {
                    "path": "/http-server/ping",
                    "threadCoreSize": 2,
                    "timeoutInMilliseconds": 1000,
                    "timeoutEnabled": true,
                    "slidingWindowDuration": 30000,
                    "numberOfWindowSegments": 10,
                    "requestThreshold": 4,
                    "failureRateThreshold": 50,
                    "circuitBreakerResetTime": 30000,
                    "circuitBreakerEnabled": true,
                    "fallbackResponse": "熔断时间",
                    "globalEnable": false
                }
            ]
        }
      ]
   }
   ```
   这里通过添加两个过滤器（利用prometheus收集数据）实现对请求流量的监控，并且最后通过定制化面板在grafana展示出来。
 + 用户的权限认证
   ```
   {
    "rules": [
        {
            "id": "2",
            "name": "user-private",
            "protocol": "http",
            "serviceId": "backend-user-server",
            "prefix": "/user/private",
            "paths": [
                "/user/private/user-info"
            ],
            "filterConfigs": [
                {
                    "id": "load_balance_filter",
                    "config": {
                        "load_balance": "RoundRobin"
                    }
                },
                {
                    "id": "user_auth_filter"
                },
                {
                    "id": "router_filter"
                }
            ]
        }
     ]
   }
   ```

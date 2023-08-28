package com.lls.gateway.client.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "api")
public class ApiProperties {

    private String registerAddress;

    //空格代表的就是public的命名空间id
    private String namespace = "";

    private String group = "DEFAULT_GROUP";

    private boolean gray;
}

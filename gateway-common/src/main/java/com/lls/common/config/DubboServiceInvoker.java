package com.lls.common.config;

import lombok.Getter;
import lombok.Setter;

/**
 * dubbo协议的注册服务调用模型类
 */
@Getter
@Setter
public class DubboServiceInvoker extends AbstractServiceInvoker {
	
	private String registerAddress;
	
	private String interfaceClass;
	
	private String methodName;
	
	private String[] parameterTypes;
	
	private String version;
}

package com.lls.common.config;

/**
 * 服务调用的接口模型描述
 */
public interface ServiceInvoker {

	String getInvokerPath();

	void setInvokerPath(String invokerPath);

	int getTimeout();

	void setTimeout(int timeout);
	
}

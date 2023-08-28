package com.lls.common.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 服务实例
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "serviceInstanceId")
public class ServiceInstance implements Serializable {

	private static final long serialVersionUID = -7559569289189228478L;
	
	protected String uniqueId;

	protected String serviceInstanceId;

	protected String ip;

	protected int port;

	protected String tags;

	protected Integer weight;

	protected long registerTime;

	protected boolean enable = true;

	protected String version;

	private boolean gray;
	
}

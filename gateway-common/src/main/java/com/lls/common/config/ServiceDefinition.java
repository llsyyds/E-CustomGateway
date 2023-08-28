package com.lls.common.config;

import lombok.*;

import java.io.Serializable;
import java.util.Map;

/**
 * 服务定义
 */
@Builder
@Getter
@Setter
@EqualsAndHashCode(of = "uniqueId")
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDefinition implements Serializable {

	private static final long serialVersionUID = -8263365765897285189L;

	private String uniqueId;

	private String serviceId;

	private String version;

	private String protocol;

	private String patternPath;

	private String namespace;

	private String group;

	private boolean enable = true;

	private Map<String, ServiceInvoker> invokerMap;

}

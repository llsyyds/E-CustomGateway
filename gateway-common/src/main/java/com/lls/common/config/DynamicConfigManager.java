package com.lls.common.config;


import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 动态服务缓存配置管理类
 */
public class DynamicConfigManager {

	private DynamicConfigManager() {}

	public static DynamicConfigManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder {
		private static final DynamicConfigManager INSTANCE = new DynamicConfigManager();
	}


	private ConcurrentHashMap<String /* uniqueId */ , ServiceDefinition>  serviceDefinitionMap = new ConcurrentHashMap<>();
	
	private ConcurrentHashMap<String /* uniqueId */ , Set<ServiceInstance>>  serviceInstanceMap = new ConcurrentHashMap<>();

	private ConcurrentHashMap<String /* ruleId */ , Rule>  ruleMap = new ConcurrentHashMap<>();

	private ConcurrentHashMap<String /* 路径 */ , Rule>  pathRuleMap = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String /* 服务名 */ , List<Rule>>  serviceRuleMap = new ConcurrentHashMap<>();


	/***************** 	对服务定义缓存进行操作的系列方法 	***************/
	
	public void putServiceDefinition(String uniqueId, ServiceDefinition serviceDefinition) {
		serviceDefinitionMap.put(uniqueId, serviceDefinition);;
	}

	public void removeServiceDefinition(String uniqueId) {
		serviceDefinitionMap.remove(uniqueId);
	}

	public ServiceDefinition getServiceDefinition(String uniqueId) {
		return serviceDefinitionMap.get(uniqueId);
	}

	public ConcurrentHashMap<String, ServiceDefinition> getServiceDefinitionMap() {
		return serviceDefinitionMap;
	}


	/***************** 	对服务实例缓存进行操作的系列方法 	***************/

	public void addServiceInstance(String uniqueId, Set<ServiceInstance> serviceInstanceSet) {
		serviceInstanceMap.put(uniqueId, serviceInstanceSet);
	}

	public void removeServiceInstancesByUniqueId(String uniqueId) {
		serviceInstanceMap.remove(uniqueId);
	}

	public void addServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
		Set<ServiceInstance> set = serviceInstanceMap.get(uniqueId);
		set.add(serviceInstance);
	}

	public void removeServiceInstance(String uniqueId, String serviceInstanceId) {
		Set<ServiceInstance> set = serviceInstanceMap.get(uniqueId);
		Iterator<ServiceInstance> it = set.iterator();
		while(it.hasNext()) {
			ServiceInstance is = it.next();
			if(is.getServiceInstanceId().equals(serviceInstanceId)) {
				it.remove();
				break;
			}
		}
	}

	public void updateServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
		Set<ServiceInstance> set = serviceInstanceMap.get(uniqueId);
		Iterator<ServiceInstance> it = set.iterator();
		while(it.hasNext()) {
			ServiceInstance is = it.next();
			if(is.getServiceInstanceId().equals(serviceInstance.getServiceInstanceId())) {
				it.remove();
				break;
			}
		}
		set.add(serviceInstance);
	}

	public Set<ServiceInstance> getServiceInstanceByUniqueId(String uniqueId,boolean gray){
		Set<ServiceInstance> serviceInstances = serviceInstanceMap.get(uniqueId);
		if (CollectionUtils.isEmpty(serviceInstances)){
			return Collections.emptySet();
		}
		return serviceInstances.stream()
				.filter(serviceInstance -> serviceInstance.isGray() == gray)
				.collect(Collectors.toSet());
	}


	/***************** 	对规则缓存进行操作的系列方法 	***************/
	
	public void putRule(String ruleId, Rule rule) {
		ruleMap.put(ruleId, rule);
	}

	public void putAllRule(List<Rule> ruleList) {
		ConcurrentHashMap<String,Rule> newRuleMap = new ConcurrentHashMap<>();
		ConcurrentHashMap<String,Rule> newPathMap = new ConcurrentHashMap<>();
		ConcurrentHashMap<String,List<Rule>> newServiceMap = new ConcurrentHashMap<>();
		for(Rule rule : ruleList){
			newRuleMap.put(rule.getId(),rule);
			List<Rule> rules = newServiceMap.get(rule.getServiceId());
			if(rules == null){
				rules = new ArrayList<>();
			}
			rules.add(rule);
			newServiceMap.put(rule.getServiceId(),rules);

			List<String> paths = rule.getPaths();
			for(String path :paths){
				String key = rule.getServiceId()+"."+path;
				newPathMap.put(key,rule);
			}
		}
		ruleMap = newRuleMap;
		pathRuleMap = newPathMap;
		serviceRuleMap = newServiceMap;
	}
	
	public Rule getRule(String ruleId) {
		return ruleMap.get(ruleId);
	}
	
	public void removeRule(String ruleId) {
		ruleMap.remove(ruleId);
	}
	
	public ConcurrentHashMap<String, Rule> getRuleMap() {
		return ruleMap;
	}

	public Rule  getRuleByPath(String path){
		return pathRuleMap.get(path);
	}

	public List<Rule>  getRuleByServiceId(String serviceId){
		return serviceRuleMap.get(serviceId);
	}
}

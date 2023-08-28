package com.lls.common.config;

import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 规则对象
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Rule implements Comparable<Rule>, Serializable {

    private String id;

    private String name;

    private String protocol;

    private String  serviceId;

    private String prefix;

    private List<String> paths;

    private Integer order;

    private Set<FilterConfig> filterConfigs = new HashSet<>();

    private RetryConfig retryConfig = new RetryConfig();

    private Set<FlowCtlConfig> flowCtlConfigs =new HashSet<>();

    private Set<HystrixConfig> hystrixConfigs = new HashSet<>();

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    public static class FilterConfig{

        private String id;

        private String config;

    }

    @Getter
    @Setter
    public static class RetryConfig{
        private int times;

    }

    @Getter
    @Setter
    public static class FlowCtlConfig{

        private String type;

        private String value;

        private String model;

        private String config;

    }

    @Getter
    @Setter
    public static class HystrixConfig{
        private String path;

        private int threadCoreSize;

        private int timeoutInMilliseconds;

        private boolean timeoutEnabled;

        private int slidingWindowDuration;

        private int numberOfWindowSegments;

        private int requestThreshold;

        private int failureRateThreshold;

        private int circuitBreakerResetTime;

        private boolean circuitBreakerEnabled;

        private String  fallbackResponse;

        private boolean globalEnable;
    }

    public  boolean addFilterConfig(FilterConfig filterConfig){
        return filterConfigs.add(filterConfig);
    }

    public  FilterConfig getFilterConfig(String id){
        for(FilterConfig filterConfig : filterConfigs){
            if(filterConfig.getId().equalsIgnoreCase(id)){
                return filterConfig;
            }
        }
        return null;
    }

    public  boolean hashId(String id){
        for(FilterConfig filterConfig : filterConfigs){
            if(filterConfig.getId().equalsIgnoreCase(id)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(Rule o) {
        int orderCompare = Integer.compare(getOrder(),o.getOrder());
        if (orderCompare ==0 ){
            return  getId().compareTo(o.getId());
        }
        return orderCompare;
    }
}


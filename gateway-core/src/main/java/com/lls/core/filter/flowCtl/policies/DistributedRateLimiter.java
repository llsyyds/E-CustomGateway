package com.lls.core.filter.flowCtl.policies;

import com.lls.core.redis.JedisUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 使用Redis实现分布式限流
 */
@Slf4j
public class DistributedRateLimiter {

    protected JedisUtil jedisUtil;

    public DistributedRateLimiter(JedisUtil jedisUtil) {
        this.jedisUtil = jedisUtil;
    }

    private static  final  int SUCCESS_RESULT = 1;
    private static  final  int FAILED_RESULT = 0;

    /**
     * 执行分布式限流
     * @param key
     * @param limit
     * @param expire
     * @return
     */
    public  boolean doFlowCtl(String key,int limit,int expire){
        try {
            Object object = jedisUtil.executeScript(key,limit,expire);
            if(object == null){
                return true;
            }
            Long result = Long.valueOf(object.toString());
            if(FAILED_RESULT == result){
                return  false;
            }
        }catch (Exception e){
            throw  new RuntimeException("分布式限流发生错误");
        }
        return true;
    }




}

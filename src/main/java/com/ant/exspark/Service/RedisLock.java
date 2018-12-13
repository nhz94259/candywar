package com.ant.exspark.Service;

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


@Slf4j
@Component
public class RedisLock {

    @Autowired private StringRedisTemplate redisTemplate;


    public  boolean lock(String Key,String Value){

        if (redisTemplate.opsForValue().setIfAbsent(Key,Value))
        {
            return  true;
        }
        String currentValue = redisTemplate.opsForValue().get(Key);

        if(!StringUtils.isEmpty(currentValue)&&Long.parseLong(currentValue)<System.currentTimeMillis())
        {
            //获取上一个锁的时间
            String oldValue = redisTemplate.opsForValue().getAndSet(Key,Value);
            if(!StringUtils.isEmpty(oldValue)&&oldValue.equals(currentValue))
            {
                return  true;
            }
        }
        return false;
    }

    public void unlock(String Key, String Value){
        try {
            String currentValue = redisTemplate.opsForValue().get(Key);
            if(!StringUtils.isEmpty(currentValue)&&currentValue.equals(Value)){
                redisTemplate.opsForValue().getOperations().delete(Key);
            }
        }catch (Exception e){
            log.error("redis 分布式锁解锁异常：：{}",e);
        }

    }
}

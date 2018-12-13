package com.ant.exspark.controller;


import com.ant.exspark.Service.RedisLock;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class CandyController {

    private  static  long TIMEOUT = 10*1000;

    @Autowired private  CandyStock candyStock;
    @Autowired private RedisLock redisLock;

    @GetMapping(path = "/getcandy")
    public String get(){
        //设置value 值 为预计超时时间
        String res=discreateStock("candy",String.valueOf(System.currentTimeMillis()+TIMEOUT));
        log.info(res);
        return " get candy success! "+res;
    }

    @GetMapping(path = "/showstock")
    public String showStock(){
        return  "current stock :"+candyStock.getStockNum().toString();
    }

    private String discreateStock(String key ,String value){
        if(redisLock.lock(key, value)){
            return "oh , too much request ,try again!";
        }
        candyStock.disStock();
        redisLock.unlock(key, value);
        return "nice , git it!";
    }

    //init
    @GetMapping(path = "/init")
    public String init(){
        return candyStock.init();
    }



    @Data
    @Component
    public class CandyStock {
        private Integer stockNum=1000;

        //减少糖果 1.
        public Integer disStock(){
            this.setStockNum(stockNum-1);
            return this.stockNum;
        }

        public String init(){
            setStockNum(1000);
            return  "init finised, candy stock is :" +this.getStockNum();
        }

    }

    @Autowired private StringRedisTemplate redisTemplate;
    @GetMapping(path = "/testredis")
    public String testRedis(){return  redisTemplate.opsForValue().setIfAbsent("hehe","1").toString(); }
}

package org.example.smartmallbackend.service.impl;


import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.smartmallbackend.common.Result;
import org.example.smartmallbackend.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeckillServiceImpl {
    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private DefaultRedisScript<Long> seckillScript;

    //初始化加载lua脚本
    @PostConstruct
    public void init(){
        seckillScript = new DefaultRedisScript<>();
        seckillScript.setResultType(Long.class);
        seckillScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("seckill_stock.lua")));
    }
    /**
     * 库存预热
     */
    public void prepareStock(Long skuId, int stock) {
        redisTemplate.opsForValue().set("seckill:stock:" + skuId, String.valueOf(stock));
        redisTemplate.delete("seckill:stock:" + skuId);//清除旧的购买记录
    }
    public Result<String> seckill(Long skuId,Long userId) {
        //redis key 定义
        String stockKey = "seckill:stock:" + skuId;
        String userKey = "seckill:bought:" + skuId;
        //执行lua脚本
        List<String> keys= Arrays.asList(stockKey,userKey);
        Long result = redisTemplate.execute(seckillScript, keys, String.valueOf(userId),"1");
        //1-成功，0-库存不足，-1-重复购买
        if(result==null||result==0) {
            return Result.error("库存不足");
        }
        if(result==-1) {
            return Result.error("不能重复购买");
        }
        //发送异步下单消息
        Map<String,Object> msg=new HashMap<>();
        msg.put("count",1);
        msg.put("userId",userId);
        msg.put("skuId",skuId);
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.SECKILL_EXCHANGE,
                RabbitMqConfig.SECKILL_ROUTING_KEY,
                JSONUtil.toJsonStr(msg)
        );
        return Result.success("秒杀成功，订单正在处理中");
    }

}

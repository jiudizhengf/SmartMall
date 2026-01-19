package org.example.smartmallbackend.config;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        // 1. 自定义 ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // 注册 JavaTimeModule 以支持 LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());

        // (可选) 配置一下 GenericJackson2JsonRedisSerializer 需要的类型识别
        // 虽然 GenericJackson2JsonRedisSerializer 构造函数里也会设，但传了 Mapper 最好自己显式声明一下策略
        // 这里的配置是为了让 Redis 存入 @class 属性，反序列化时知道是哪个实体类
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // 2. 创建带自定义 Mapper 的序列化器
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        // 1. 全局默认配置
        RedisCacheConfiguration defaultConf = RedisCacheConfiguration.defaultCacheConfig()
                // 键序列化：String
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 值序列化：JSON (GenericJackson2JsonRedisSerializer 自动处理多态)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                // 默认过期时间：60 分钟
                .entryTtl(Duration.ofMinutes(60))
                // 不缓存 null 值 (防止缓存穿透的一种简单手段，但高级用法是缓存空对象并设短 TTL)
                .disableCachingNullValues();

        // 2. 针对特定缓存名称的个性化配置 (可选)
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        // 例如：首页广告 (sms:home:ads) 只缓存 10 分钟
        configMap.put("sms:home:ads", defaultConf.entryTtl(Duration.ofMinutes(10)));
        // 例如：商品详情 (pms:spu) 缓存 2 小时
        configMap.put("pms:spu", defaultConf.entryTtl(Duration.ofHours(2)));
        //库存信息 (pms:sku) 缓存 30 分钟
        configMap.put("pms:sku", defaultConf.entryTtl(Duration.ofMinutes(30)));
        // 3. 构建 CacheManager
        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConf)
                .withInitialCacheConfigurations(configMap)
                .build();

    }
}

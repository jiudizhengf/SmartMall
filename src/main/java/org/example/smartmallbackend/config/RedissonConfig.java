package org.example.smartmallbackend.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private String post;
    @Value("${spring.data.redis.password:}")
    private String password;

    @Bean
    public RedissonClient redissonClient() {
        // 配置 RedissonClient 连接到 Redis 服务器的代码
        Config config = new Config();
        String address = "redis://" + host + ":" + post;
        config.useSingleServer().setAddress(address)
                .setPassword(password!=null&&!password.isEmpty()?password:null)
                .setDatabase(0);
        return Redisson.create(config);// 返回实际的 RedissonClient 实例
    }
}

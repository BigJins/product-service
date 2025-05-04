package org.allmart.productservice.infrastructure.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;



@Configuration
public class StockRedisConfig {

    private final StockRedisProperties props;

    public StockRedisConfig(StockRedisProperties props) {
        this.props = props;
    }

    @Bean
    public LettuceConnectionFactory stockRedisConnectionFactory() {
        // 1) sentinel 구성
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
        sentinelConfig.setMaster(props.getMaster());
        props.getNodes().forEach(hostPort -> {
            String[] hp = hostPort.split(":");
            sentinelConfig.sentinel(hp[0], Integer.parseInt(hp[1]));
        });

        // 2) LettuceConnectionFactory 생성
        return new LettuceConnectionFactory(sentinelConfig);
    }

    @Bean(name = "stockRedisTemplate")
    public RedisTemplate<String, Long> stockRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Long> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Long.class)); // 여기만 바꿈

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}

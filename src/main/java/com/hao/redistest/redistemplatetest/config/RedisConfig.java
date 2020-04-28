package com.hao.redistest.redistemplatetest.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * @author hao
 * @date 2020/4/27
 */
@Configuration
public class RedisConfig {

    //redis连接池参数设置
    @Bean
    @ConfigurationProperties(prefix = "spring.redis.lettuce.pool")
    public GenericObjectPoolConfig redisPoolConfig() {
        return new GenericObjectPoolConfig();
    }

    //根据配置文件的mac-resource读取 reids资源配置
    @Bean
    @ConfigurationProperties(prefix = "spring.redis.mac-resource")
    public RedisStandaloneConfiguration macResourceConfiguration() {
        return new RedisStandaloneConfiguration();
    }

    //使用lettuceConnectionFactory连接redis
    @Bean
    public LettuceConnectionFactory macResourceFactory() {
        GenericObjectPoolConfig redisPoolConfig = redisPoolConfig();
        LettuceClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.builder()
                .poolConfig(redisPoolConfig).commandTimeout(Duration.ofMillis(redisPoolConfig.getMaxWaitMillis())).build();
        return new LettuceConnectionFactory(macResourceConfiguration(), clientConfiguration);
    }

    //配置RedisTemplate
    @Bean("macResourceRedisTemplate")
    public RedisTemplate<String,String> macResourceRedisTemplate() {
        LettuceConnectionFactory macResourcePoolFactory = macResourceFactory();
        RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);
        redisTemplate.setConnectionFactory(macResourcePoolFactory);
        return redisTemplate;
    }
}

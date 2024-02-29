package io.agora.uikit.config;

import io.agora.uikit.utils.RedisUtil;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.integration.redis.util.RedisLockRegistry;

import java.time.Duration;

@Configuration
@AllArgsConstructor
public class RedisConfig {
    private final RedisConnectionFactory redisConnectionFactory;
    private static final String LOCK_REGISTRY_KEY = "lock";
    private static final Duration LOCK_RELEASE_TIME_DURATION = Duration.ofSeconds(30);

    @Bean
    public HashOperations<String, String, Object> hashOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForHash();
    }

    @Bean
    public ListOperations<String, Object> listOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForList();
    }

    @Bean
    public RedisTemplate<String, Object> redisCacheTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory) {
        return new RedisLockRegistry(redisConnectionFactory, LOCK_REGISTRY_KEY, LOCK_RELEASE_TIME_DURATION.toMillis());
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setHashKeySerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setHashValueSerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setEnableTransactionSupport(true);

        redisTemplate.setConnectionFactory(setRedisConnectionFactory(redisConnectionFactory));
        return redisTemplate;
    }

    @Bean
    public RedisUtil redisUtil(StringRedisTemplate stringRedisTemplate, RedisTemplate<String, Object> redisTemplate,
            RedisLockRegistry redisLockRegistry) {
        return new RedisUtil(stringRedisTemplate, redisTemplate, redisLockRegistry);
    }

    private RedisConnectionFactory setRedisConnectionFactory(RedisConnectionFactory redisConnectionFactory) {
        return redisConnectionFactory;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.setConnectionFactory(setRedisConnectionFactory(redisConnectionFactory));
        return redisTemplate;
    }

    @Bean
    public CacheManager redisCacheConfiguration() {
        RedisCacheConfiguration cfg = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(redisConnectionFactory)
                .withCacheConfiguration("IMToken", cfg.entryTtl(Duration.ofDays(30)))
                .withCacheConfiguration("IMUser", cfg.entryTtl(Duration.ofDays(30)))
                .withCacheConfiguration("chatRoomAPIAppToken", cfg.entryTtl(Duration.ofDays(30)))
                .withCacheConfiguration("chatRoomAPIUserToken", cfg.entryTtl(Duration.ofDays(30)))
                .cacheDefaults(cfg)
                .build();
    }
}
package io.agora.uikit.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.integration.redis.util.RedisLockRegistry;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class RedisUtil {
    private StringRedisTemplate stringRedisTemplate;
    private RedisTemplate<String, Object> redisTemplate;
    private final RedisLockRegistry redisLockRegistry;

    // Distributed lock start
    public void lock(String lockKey) {
        Lock lock = obtainLock(lockKey);
        lock.lock();
    }

    private Lock obtainLock(String lockKey) {
        return redisLockRegistry.obtain(lockKey);
    }

    public boolean tryLock(String lockKey) {
        Lock lock = obtainLock(lockKey);
        return lock.tryLock();
    }

    public boolean tryLock(String lockKey, long waitSecond) {
        Lock lock = obtainLock(lockKey);
        try {
            return lock.tryLock(waitSecond, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("tryLock exception, lockKey:{}, waitSecond:{}, error:", lockKey, waitSecond, e);
            return false;
        }
    }

    public boolean unlock(String lockKey) {
        try {
            Lock lock = obtainLock(lockKey);
            lock.unlock();
        } catch (Exception e) {
            log.error("unlock exception, lockKey:{}, error:", lockKey, e);
            return false;
        }
        return true;
    }
    // Distributed lock end

    /**
     * Delete
     *
     * @param key
     */
    public boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * Get
     *
     * @param key
     * @return
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * Get cache expiration time
     *
     * @param key
     * @return
     */
    public long getExpire(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * Set cache expiration time
     * 
     * @param key
     * @param expire
     * @return
     */
    public boolean expire(String key, long expire) {
        return stringRedisTemplate.expire(key, expire, TimeUnit.SECONDS);
    }

    /**
     * Hash delete
     *
     * @param key
     * @param hashKeys
     * @return
     */
    public Long hashDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    /**
     * Hash entries
     *
     * @param key
     * @return
     */
    public Map<Object, Object> hashEntries(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * Hash get
     *
     * @param key
     * @param hashKey
     * @return
     */
    public Object hashGet(String key, Object hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * Hash hasKey
     *
     * @param key
     * @param hashKey
     * @return
     */
    public Boolean hashHasKey(String key, Object hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    /**
     * Hash multiGet
     *
     * @param key
     * @param hashKeys
     * @return
     */
    public List<Object> hashMultiGet(String key, Collection<Object> hashKeys) {
        return redisTemplate.opsForHash().multiGet(key, hashKeys);
    }

    /**
     * Hash keys
     *
     * @param key
     * @return
     */
    public Set<Object> hashKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    /**
     * Hash put
     *
     * @param key
     * @param hashKey
     * @param value
     * @return
     */
    public void hashPut(String key, Object hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * Hash putIfAbsent
     *
     * @param key
     * @param hashKey
     * @param value
     * @return
     */
    public Boolean hashPutIfAbsent(String key, Object hashKey, Object value) {
        return redisTemplate.opsForHash().putIfAbsent(key, hashKey, value);
    }

    /**
     * Hash size
     *
     * @param key
     * @return
     */
    public Long hashSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * Hash values
     *
     * @param key
     * @return
     */
    public List<Object> hashValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    /**
     * Check whether a key exists
     *
     * @param key
     * @return
     */
    public boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * Set
     *
     * @param key
     * @param val
     * @param expire
     * @return
     */
    public void set(String key, String val, long expire) {
        stringRedisTemplate.opsForValue().set(key, val, expire, TimeUnit.SECONDS);
    }

    /**
     * Zset add
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    public Boolean zsetAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * Zset add if it does not already exists
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    public Boolean zsetAddIfAbsent(String key, Object value, double score) {
        return redisTemplate.opsForZSet().addIfAbsent(key, value, score);
    }

    /**
     * Zset add
     *
     * @param key
     * @return
     */
    public TypedTuple<Object> zsetPopMax(String key) {
        return redisTemplate.opsForZSet().popMax(key);
    }

    /**
     * Zset range
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<Object> zsetRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * Zset range by score
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Set<Object> zsetRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * Zset range reverse
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<Object> zsetRangeReverse(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * Zset remove
     *
     * @param key
     * @param values
     * @return
     */
    public Long zsetRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * Zset size
     *
     * @param key
     * @return
     */
    public Long zsetSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }
}
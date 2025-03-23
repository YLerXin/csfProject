package vttp.batchb.csf.project.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRedisRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REDIS_HASH = "LOGIN";

    public UserRedisRepository(@Qualifier("redis-string") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveSession(String email, String token) {
        redisTemplate.opsForHash().put(REDIS_HASH, email, token);
    }

    public String getSessionToken(String email) {
        Object val = redisTemplate.opsForHash().get(REDIS_HASH, email);
        return (val != null) ? val.toString() : null;
    }

    public boolean hasSession(String email) {
        return redisTemplate.opsForHash().hasKey(REDIS_HASH, email);
    }

    public void removeSession(String email) {
        redisTemplate.opsForHash().delete(REDIS_HASH, email);
    }
}
package ks.com.budgetmanagementproject.feature.token.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisBlackTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final String REDIS_BLACKLIST_KEY_PREFIX = "blacklist:";

    public RedisBlackTokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addBlacklistedToken(String token, long expiration) {
        String key = REDIS_BLACKLIST_KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, token, expiration, TimeUnit.MILLISECONDS);
    }

    public boolean isTokenBlacklisted(String token) {
        String key = REDIS_BLACKLIST_KEY_PREFIX + token;
        String storedToken = redisTemplate.opsForValue().get(key);
        return storedToken != null;
    }
}
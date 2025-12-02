package ks.com.budgetmanagementproject.feature.token.service;

import ks.com.budgetmanagementproject.feature.token.entity.RefreshToken;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisRefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final String REDIS_REFRESH_TOKEN_KEY_PREFIX = "refreshToken:";

    public RedisRefreshTokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addRefreshToken(RefreshToken refreshToken, long expiration) {
        String key = REDIS_REFRESH_TOKEN_KEY_PREFIX + refreshToken.getRefresh();
        redisTemplate.opsForValue().set(key, refreshToken.getUsername(), expiration, TimeUnit.MILLISECONDS);
    }

    public void addRefreshToken(String refreshToken, long expiration) {
        String key = REDIS_REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(key, refreshToken, expiration, TimeUnit.MILLISECONDS);
    }

    public boolean isRefreshTokenValid(String refreshToken) {
        String key = REDIS_REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        String storedValue = redisTemplate.opsForValue().get(key);
        return storedValue != null;
    }

    public void deleteRefreshToken(String refreshToken) {
        String key = REDIS_REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        redisTemplate.delete(key);
    }

    public String getUsernameByRefreshToken(String refreshToken) {
        String key = REDIS_REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        return redisTemplate.opsForValue().get(key);
    }
}
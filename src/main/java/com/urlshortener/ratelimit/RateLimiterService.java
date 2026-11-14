package com.urlshortener.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    private final StringRedisTemplate redisTemplate;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RateLimitResult checkLimit(String clientIp) {
        // Calculate the current minute window
        long currentMinute = System.currentTimeMillis() / 60000;
        String key = RATE_LIMIT_PREFIX + clientIp + ":" + currentMinute;

        // Atomically increment the counter for this IP + window
        Long currentCount = redisTemplate.opsForValue().increment(key);
        if (currentCount != null && currentCount == 1) {
            // First request in this window - set expiry
            redisTemplate.expire(key, WINDOW);
        }

        // Calculate remaining requests and whether this one is allowed
        long remaining = Math.max(0, MAX_REQUESTS - (currentCount != null ? currentCount : 0));
        boolean allowed = currentCount != null && currentCount <= MAX_REQUESTS;
        long resetSeconds = 60 - (System.currentTimeMillis() / 1000 % 60);

        return new RateLimitResult(allowed, MAX_REQUESTS, remaining, resetSeconds);
    }
}
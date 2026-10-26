package com.student.urlshortener.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redis;
    private final int maxRequests;
    private final int windowSeconds;

    public RateLimiterService(StringRedisTemplate redis,
                              @Value("${app.rate-limit.requests-per-minute:60}") int maxRequests,
                              @Value("${app.rate-limit.window-seconds:60}") int windowSeconds) {
        this.redis = redis;
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    public boolean isAllowed(String clientIp) {
        String key = "rate:" + clientIp;

        Long count = redis.opsForValue().increment(key);

        if (count == null) {
            return true;
        }

        if (count == 1) {
            redis.expire(key, Duration.ofSeconds(windowSeconds));
        }

        return count <= maxRequests;
    }
}

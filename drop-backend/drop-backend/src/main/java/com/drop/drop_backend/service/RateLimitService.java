package com.drop.drop_backend.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(
            StringRedisTemplate redisTemplate
    ) {
        this.redisTemplate =
                redisTemplate;
    }

    // =========================================================
    // CHECK RATE LIMIT
    // =========================================================

    public boolean isAllowed(
            String clientIp,
            String endpoint,
            int maxRequests
    ) {

        if (
                clientIp == null
                        || clientIp.isBlank()
        ) {
            clientIp = "unknown";
        }

        if (
                endpoint == null
                        || endpoint.isBlank()
        ) {
            endpoint = "unknown";
        }

        String key =
                "drop:rate:"
                        + endpoint
                        + ":"
                        + clientIp;

        try {

            Long count =
                    redisTemplate.opsForValue()
                            .increment(key);

            if (
                    count != null
                            && count == 1
            ) {

                redisTemplate.expire(
                        key,
                        Duration.ofMinutes(1)
                );
            }

            return count != null
                    && count <= maxRequests;

        } catch (Exception ignored) {

            // Redis failure should NOT bring
            // the entire DROP API down.

            return true;
        }
    }
}
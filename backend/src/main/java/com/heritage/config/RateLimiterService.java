package com.heritage.config;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterService {
    private static class Counter {
        long windowStartMs;
        int count;
    }

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public synchronized void check(String key, int limit, long windowMs) {
        long now = Instant.now().toEpochMilli();
        Counter c = counters.computeIfAbsent(key, k -> new Counter());
        if (now - c.windowStartMs > windowMs) {
            c.windowStartMs = now;
            c.count = 0;
        }
        c.count++;
        if (c.count > limit) {
            throw new IllegalStateException("请求过于频繁");
        }
    }
}

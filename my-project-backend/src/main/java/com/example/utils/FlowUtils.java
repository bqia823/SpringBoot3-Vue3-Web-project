package com.example.utils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * General utility for rate limiting
 * Performs rate limiting operations for different situations
 * supports rate limit escalation
 */
@Slf4j
@Component
public class FlowUtils {

    @Resource
    StringRedisTemplate template;

    /**
     * Single frequency limit. After a successful request
     * further requests are not allowed during the cool down period
     * e.g., no requests within 3 seconds
     *
     * @param key       the key
     * @param blockTime the block time
     * @return whether the rate limit check passed
     */
    public boolean limitOnceCheck(String key, int blockTime) {
        return this.internalCheck(key, 1, blockTime, (overclock) -> false);
    }

    /**
     * Single frequency limit. After a successful request, further requests
     * are not allowed during the cool down period
     * e.g., no requests within 3 seconds. If requests continue, the block
     * time is extended
     *
     * @param key         the key
     * @param frequency   the request frequency
     * @param baseTime    the base block time
     * @param upgradeTime the extended block time
     * @return whether the rate limit check passed
     */
    public boolean limitOnceUpgradeCheck(String key, int frequency, int baseTime, int upgradeTime) {
        return this.internalCheck(key, frequency, baseTime, (overclock) -> {
            if (overclock)
                template.opsForValue().set(key, "1", upgradeTime, TimeUnit.SECONDS);
            return false;
        });
    }

    /**
     * Limits the number of requests within a time period
     * e.g., limit 20 requests within 3 seconds, block if exceeded
     *
     * @param counterKey the counter key
     * @param blockKey   the block key
     * @param blockTime  the block time
     * @param frequency  the request frequency
     * @param period     the counting period
     * @return whether the rate limit check passed
     */
    public boolean limitPeriodCheck(String counterKey, String blockKey, int blockTime, int frequency, int period) {
        return this.internalCheck(counterKey, frequency, period, (overclock) -> {
            if (overclock)
                template.opsForValue().set(blockKey, "", blockTime, TimeUnit.SECONDS);
            return !overclock;
        });
    }

    /**
     * Main logic for internal rate limit checking
     *
     * @param key       the counter key
     * @param frequency the request frequency
     * @param period    the counting period
     * @param action    the limit action and strategy
     * @return whether the rate limit check passed
     */
    private boolean internalCheck(String key, int frequency, int period, LimitAction action) {
        String count = template.opsForValue().get(key);
        if (count != null) {
            long value = Optional.ofNullable(template.opsForValue().increment(key)).orElse(0L);
            int c = Integer.parseInt(count);
            if (value != c + 1)
                template.expire(key, period, TimeUnit.SECONDS);
            return action.run(value > frequency);
        } else {
            template.opsForValue().set(key, "1", period, TimeUnit.SECONDS);
            return true;
        }
    }

    /**
     * Internal use, limit action and strategy
     */
    private interface LimitAction {
        boolean run(boolean overclock);
    }
}

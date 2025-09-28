package url.shortener.challenge.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RateLimiter {
    private final StringRedisTemplate redis;

    public RateLimiter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean allow(String key, int maxPerMinute) {
        String redisKey = "rate:" + key + ":" + (System.currentTimeMillis() / 60000);
        Long count = redis.opsForValue().increment(redisKey);
        if (count == 1) {
            redis.expire(redisKey, Duration.ofMinutes(2));
        }
        return count <= maxPerMinute;
    }
}

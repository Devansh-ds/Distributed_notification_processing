package com.devansh.rateLimit;

import com.devansh.exception.IngressRateLimitException;
import com.devansh.rateLimit.algorithms.RateLimiter;
import com.devansh.rateLimit.entity.RateLimitConfig;
import com.devansh.rateLimit.entity.RateLimitStats;
import com.devansh.rateLimit.entity.RateLimiterType;
import org.springframework.stereotype.Component;

@Component
public class IngressGuard {

    private RateLimiter rateLimiter;

    public IngressGuard() {
        this.rateLimiter = RateLimitFactory.create(
                RateLimiterType.TOKEN_BUCKET,
                RateLimitConfig.builder()
                        .bucketCapacity(2000)
                        .refillToken(300)
                        .refillIntervalMillis(1000)
                .build()
        );
    }

    public void check() throws IngressRateLimitException {
        if (!rateLimiter.isAllowed()) {
            throw new IngressRateLimitException("Rate Limit Exceeded");
        }
    }

    public RateLimitStats getRateLimitStats() {
        return rateLimiter.getStats();
    }
}

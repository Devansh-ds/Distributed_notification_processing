package com.devansh.metrics;

import com.devansh.rateLimit.IngressGuard;
import com.devansh.rateLimit.entity.RateLimitStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
public class RateLimiterReporter {

    private final IngressGuard ingressGuard;

    public RateLimiterReporter(IngressGuard ingressGuard) {
        this.ingressGuard = ingressGuard;
    }

    @Scheduled(fixedDelay = 1000)
    public void report() {
        RateLimitStats stats = ingressGuard.getRateLimitStats();
        log.info("RATE LIMIT STATS: allowed={} req/s, rejected={} req/s", stats.drainAllowed(), stats.drainRejected());
    }
}

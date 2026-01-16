package com.devansh.rateLimit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RateLimitConfig {

    private int limit;

    private int windowMillis;

    private int bucketCapacity;
    private int refillToken;
    private long refillIntervalMillis;

}

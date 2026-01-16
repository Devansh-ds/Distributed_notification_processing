package com.devansh.rateLimit.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RateLimitStats {

    private AtomicLong requestsAllowed;
    private AtomicLong requestsNotAllowed;

    public RateLimitStats(AtomicLong requestsAllowed, AtomicLong requestsNotAllowed) {
        this.requestsAllowed = requestsAllowed;
        this.requestsNotAllowed = requestsNotAllowed;
    }

    public long drainAllowed() {
        return requestsAllowed.getAndSet(0);
    }

    public long drainRejected() {
        return requestsNotAllowed.getAndSet(0);
    }

}

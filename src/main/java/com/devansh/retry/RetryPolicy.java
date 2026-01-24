package com.devansh.retry;

public class RetryPolicy {

    private RetryPolicy() {}

    public static final int MAX_RETRIES = 3;
    public static final int MAX_ENQUEUE_RETRIES = 5;


    public static long backOffMillis(int retryCount) {
        return (long) Math.pow(2, retryCount) * 1000;
    }

    public static long enqueueBackoffMillis(int enqueueRetry) {
        return 500L * enqueueRetry; // linear
    }

}

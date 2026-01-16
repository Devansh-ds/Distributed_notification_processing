package com.devansh.exception;

public class IngressRateLimitException extends Exception {
    public IngressRateLimitException() {
        super();
    }
    public IngressRateLimitException(String message) {
        super(message);
    }
}

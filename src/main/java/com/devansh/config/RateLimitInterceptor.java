package com.devansh.config;

import com.devansh.rateLimit.IngressGuard;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final IngressGuard ingressGuard;

    public RateLimitInterceptor(IngressGuard ingressGuard) {
        this.ingressGuard = ingressGuard;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        ingressGuard.check();
        return true;
    }

}

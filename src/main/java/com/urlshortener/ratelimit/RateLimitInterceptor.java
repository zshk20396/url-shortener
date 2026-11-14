package com.urlshortener.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    public RateLimitInterceptor(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Only rate limit the POST /api/shorten endpoint
        if (!"POST".equalsIgnoreCase(request.getMethod()) || !"/api/shorten".equals(request.getRequestURI())) {
            return true;
        }

        // Check the rate limit for this client's IP
        String clientIp = request.getRemoteAddr();
        RateLimitResult result = rateLimiterService.checkLimit(clientIp);

        // Always add rate limit headers to the response
        response.setHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.getResetSeconds()));

        // If limit exceeded, return 429 and block the request
        if (!result.isAllowed()) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(result.getResetSeconds()));
            response.getWriter().write("{\"error\": \"Rate limit exceeded. Try again later.\"}");
            response.setContentType("application/json");
            return false;
        }

        return true;
    }
}
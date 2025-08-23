package com.gooners.watguessr.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * AOP Aspect for handling @RateLimit annotations.
 * Intercepts method calls and applies rate limiting using Bucket4j.
 */
@Aspect
@Component
public class RateLimitAspect {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public RateLimitAspect() {
        // Constructor
    }

    @Around("@annotation(rateLimit)")
    public Object handleRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = resolveKey(rateLimit);
        Bucket bucket = cache.computeIfAbsent(key, k -> createBucket(rateLimit));

        if (bucket.tryConsume(1)) {
            // Request allowed - proceed with method execution
            return joinPoint.proceed();
        } else {
            // Rate limit exceeded - return 429 Too Many Requests
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error", "Rate limit exceeded",
                            "message", rateLimit.message(),
                            "retryAfter", "Please try again later"));
        }
    }

    private String resolveKey(RateLimit rateLimit) {
        String prefix = rateLimit.keyStrategy().name() + ":";

        switch (rateLimit.keyStrategy()) {
            case IP_ADDRESS:
                return prefix + getClientIpAddress();
            case USER_ID:
                return prefix + getUserId();
            case GLOBAL:
                return prefix + "global";
            default:
                return prefix + "unknown";
        }
    }

    private String getClientIpAddress() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            return jwt.getSubject(); // username from JWT
        }

        // Fallback to IP address if user is not authenticated
        return getClientIpAddress();
    }

    private Bucket createBucket(RateLimit rateLimit) {
        Duration duration = Duration.of(rateLimit.timeWindow(), rateLimit.timeUnit().toChronoUnit());
        Bandwidth bandwidth = Bandwidth.classic(rateLimit.requests(),
                Refill.intervally(rateLimit.requests(), duration));

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
}

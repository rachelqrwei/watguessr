package com.gooners.watguessr.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Annotation for applying rate limiting to controller methods.
 * Uses Bucket4j for token bucket rate limiting.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Number of requests allowed per time window
     */
    int requests() default 10;

    /**
     * Time window duration
     */
    int timeWindow() default 1;

    /**
     * Time unit for the time window
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;

    /**
     * Rate limit key strategy - how to identify users for rate limiting
     */
    KeyStrategy keyStrategy() default KeyStrategy.IP_ADDRESS;

    /**
     * Custom error message when rate limit is exceeded
     */
    String message() default "Rate limit exceeded. Please try again later.";

    enum KeyStrategy {
        IP_ADDRESS, // Rate limit by IP address
        USER_ID, // Rate limit by authenticated user ID (requires JWT)
        GLOBAL // Global rate limit (single bucket for all users)
    }
}

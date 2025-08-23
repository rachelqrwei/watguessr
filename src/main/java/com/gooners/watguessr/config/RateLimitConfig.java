package com.gooners.watguessr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration class to enable AOP for rate limiting.
 */
@Configuration
@EnableAspectJAutoProxy
public class RateLimitConfig {
    // AOP configuration for rate limiting aspects
}

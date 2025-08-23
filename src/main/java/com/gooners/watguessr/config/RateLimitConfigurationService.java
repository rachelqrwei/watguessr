package com.gooners.watguessr.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Service for creating Bucket4j configurations for rate limiting.
 * Provides predefined bucket configurations for different use cases.
 */
@Service
public class RateLimitConfigurationService {

    /**
     * Creates a bucket configuration with the specified parameters.
     */
    public BucketConfiguration createBucketConfiguration(int capacity, int timeWindow, TimeUnit timeUnit) {
        Duration duration = Duration.of(timeWindow, timeUnit.toChronoUnit());

        Bandwidth bandwidth = Bandwidth.classic(capacity, Refill.intervally(capacity, duration));

        return BucketConfiguration.builder()
                .addLimit(bandwidth)
                .build();
    }

    /**
     * Predefined configuration for authentication endpoints.
     * Stricter limits to prevent brute force attacks.
     */
    public BucketConfiguration authEndpointConfiguration() {
        // 5 requests per minute
        return createBucketConfiguration(5, 1, TimeUnit.MINUTES);
    }

    /**
     * Predefined configuration for game creation endpoints.
     * Moderate limits to prevent resource exhaustion.
     */
    public BucketConfiguration gameCreationConfiguration() {
        // 10 requests per minute
        return createBucketConfiguration(10, 1, TimeUnit.MINUTES);
    }

    /**
     * Predefined configuration for guess submission endpoints.
     * More lenient limits for gameplay.
     */
    public BucketConfiguration guessSubmissionConfiguration() {
        // 30 requests per minute (about 1 every 2 seconds)
        return createBucketConfiguration(30, 1, TimeUnit.MINUTES);
    }

    /**
     * Predefined configuration for general API endpoints.
     * Standard rate limiting for general use.
     */
    public BucketConfiguration generalApiConfiguration() {
        // 60 requests per minute
        return createBucketConfiguration(60, 1, TimeUnit.MINUTES);
    }

    /**
     * Predefined configuration for read-only endpoints.
     * More lenient limits for data retrieval.
     */
    public BucketConfiguration readOnlyConfiguration() {
        // 100 requests per minute
        return createBucketConfiguration(100, 1, TimeUnit.MINUTES);
    }
}

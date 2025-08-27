package com.gooners.watguessr.utils;

/**
 * Exception thrown when a rate limit is exceeded.
 * This exception is handled by the GlobalExceptionHandler to return
 * appropriate HTTP 429 Too Many Requests responses.
 */
public class RateLimitExceededException extends RuntimeException {
    
    private final String retryAfter;
    
    public RateLimitExceededException(String message) {
        super(message);
        this.retryAfter = null;
    }
    
    public RateLimitExceededException(String message, String retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }
    
    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
        this.retryAfter = null;
    }
    
    public RateLimitExceededException(String message, String retryAfter, Throwable cause) {
        super(message, cause);
        this.retryAfter = retryAfter;
    }
    
    public String getRetryAfter() {
        return retryAfter;
    }
}

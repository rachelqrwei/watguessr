package com.gooners.watguessr.utils;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // Normalize DB constraint messages to a user-friendly error
        String message = ex.getMessage();
        if (message != null && message.contains("user_email_address_key")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }
        if (message != null && message.contains("user_username_key")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid data"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        return ResponseEntity.internalServerError().body(Map.of("error", "Unexpected error: " + ex.getMessage()));
    }
}

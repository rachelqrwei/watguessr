package com.gooners.watguessr.utils;

import com.gooners.watguessr.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Get current user role from security context
     */
    private String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();



        if (auth != null && auth.getAuthorities() != null) {
            return auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_USER");
        }
        return "ROLE_USER";
    }

    /**
     * Check if current user is admin
     */
    private boolean isAdmin() {
        return getCurrentUserRole().equals("ROLE_ADMIN");
    }

    /**
     * Create error response based on user role
     */
    private ErrorResponse createErrorResponse(String code, String message, String details) {
        // register as admin

        System.out.print(getCurrentUserRole()); // undefined
        if (isAdmin()) {
            return new ErrorResponse(code, message, details);
        } else {
            return new ErrorResponse(code, message, null); // No details for normal users
        }
    }

    // ===== VALIDATION ERRORS (Always shown to users) =====
    
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        ErrorResponse response = createErrorResponse("VALIDATION_ERROR", ex.getMessage(), getStackTrace(ex));
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        ErrorResponse response = createErrorResponse("VALIDATION_ERROR", message, getStackTrace(ex));
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        
        ErrorResponse response = createErrorResponse("VALIDATION_ERROR", message, getStackTrace(ex));
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid parameter type for '" + ex.getName() + "'";
        ErrorResponse response = createErrorResponse("VALIDATION_ERROR", message, getStackTrace(ex));
        return ResponseEntity.badRequest().body(response);
    }

    // ===== DATA INTEGRITY ERRORS (User-friendly messages) =====
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        String userMessage;
        
        if (message != null && message.contains("user_email_address_key")) {
            userMessage = "Email already exists";
        } else if (message != null && message.contains("user_username_key")) {
            userMessage = "Username already exists";
        } else {
            userMessage = "Data conflict occurred";
        }
        
        ErrorResponse response = createErrorResponse("DATA_ERROR", userMessage, getStackTrace(ex));
        return ResponseEntity.badRequest().body(response);
    }

    // ===== AUTHENTICATION & AUTHORIZATION ERRORS =====
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse response = createErrorResponse("AUTH_ERROR", "Access denied", getStackTrace(ex));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ===== RATE LIMITING ERRORS =====
    
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(RateLimitExceededException ex) {
        ErrorResponse response = createErrorResponse("RATE_LIMIT_ERROR", ex.getMessage(), getStackTrace(ex));
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    // ===== SYSTEM ERRORS (Masked for normal users) =====
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String userMessage = "Something went wrong, please try again.";
        String adminMessage = ex.getMessage();
        
        ErrorResponse response = createErrorResponse("SYSTEM_ERROR", isAdmin() ? adminMessage : userMessage, getStackTrace(ex));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Get stack trace for admin users
     */
    private String getStackTrace(Exception ex) {
        if (isAdmin()) {
            StringBuilder sb = new StringBuilder();
            sb.append(ex.getClass().getSimpleName()).append(": ").append(ex.getMessage()).append("\n");
            for (StackTraceElement element : ex.getStackTrace()) {
                sb.append("\tat ").append(element.toString()).append("\n");
            }
            return sb.toString();
        }
        return null;
    }
}

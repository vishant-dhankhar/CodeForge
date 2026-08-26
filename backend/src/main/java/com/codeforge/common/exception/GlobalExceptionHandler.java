package com.codeforge.common.exception;

import com.codeforge.common.dto.ApiError;
import com.codeforge.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Resource not found: {} at path: {}", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(ApiResponse.error(ex.getErrorCode(), ex.getMessage()), ex.getHttpStatus());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(
            ConflictException ex, HttpServletRequest request) {

        log.warn("Resource conflict: {} at path: {}", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(ApiResponse.error(ex.getErrorCode(), ex.getMessage()), ex.getHttpStatus());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest request) {

        log.warn("Unauthorized access: {} at path: {}", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(ApiResponse.error(ex.getErrorCode(), ex.getMessage()), ex.getHttpStatus());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitExceeded(
            RateLimitExceededException ex, HttpServletRequest request) {

        log.warn("Rate limit exceeded at path: {}. Reason: {}", request.getRequestURI(), ex.getMessage());
        return new ResponseEntity<>(ApiResponse.error(ex.getErrorCode(), ex.getMessage()), ex.getHttpStatus());
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex, HttpServletRequest request) {

        String firstErrorMessage = ex.getBindingResult().getAllErrors().isEmpty() ?
                "Validation failed" : ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        return new ResponseEntity<>(ApiResponse.error("VALIDATION_ERROR", firstErrorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception at path: {}", request.getRequestURI(), ex);

        return new ResponseEntity<>(
                ApiResponse.error("INTERNAL_SERVER_ERROR", "An unexpected error occurred. Please try again later."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}

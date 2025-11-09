package com.evcoownership.coowner.exception;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest; // Dùng 'jakarta' cho Spring Boot 3
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private Map<String, Object> createErrorBody(String code, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("code", code);
        body.put("message", message);
        if (path != null) {
            body.put("path", path);
        }
        return body;
    }

    // Xử lý 400 Validation Error 
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> body = createErrorBody("VALIDATION_ERROR", "Dữ liệu không hợp lệ", request.getRequestURI());
        
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> 
            details.put(fe.getField(), fe.getDefaultMessage())
        );
        body.put("details", details);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Xử lý 401 Unauthorized
    @ExceptionHandler({UnauthorizedException.class, JwtException.class})
    public ResponseEntity<Map<String, Object>> handleUnauthorized(Exception ex, HttpServletRequest request) {
        String message = ex.getMessage();
        if (ex instanceof JwtException) {
            message = "Token không hợp lệ, đã hết hạn hoặc sai định dạng.";
        }
        
        Map<String, Object> body = createErrorBody("UNAUTHORIZED", message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // Xử lý 403 Forbidden 
    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<Map<String, Object>> handleForbidden(Exception ex, HttpServletRequest request) {
        String message = ex.getMessage();
        if (ex instanceof AccessDeniedException) {
            message = "Bạn không có quyền truy cập tài nguyên này.";
        }
        
        Map<String, Object> body = createErrorBody("FORBIDDEN", message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // Xử lý 404 Not Found 
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        Map<String, Object> body = createErrorBody("NOT_FOUND", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // Xử lý 409 Conflict 
    @ExceptionHandler({DataIntegrityViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleConflict(Exception ex, HttpServletRequest request) {
        String message = "Xung đột dữ liệu.";
        if (ex.getMessage() != null && ex.getMessage().contains("Unique constraint")) {
            message = "Giá trị này đã tồn tại trong hệ thống.";
        } else if (ex instanceof IllegalArgumentException) {
            message = ex.getMessage();
        }

        Map<String, Object> body = createErrorBody("CONFLICT_ERROR", message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // Xử lý 500 Internal Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest request) {
        // Luôn log lỗi 500 để debug
        logger.error("Lỗi 500 không mong muốn tại {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        Map<String, Object> body = createErrorBody("INTERNAL_ERROR", "Đã xảy ra lỗi không mong muốn phía máy chủ.", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
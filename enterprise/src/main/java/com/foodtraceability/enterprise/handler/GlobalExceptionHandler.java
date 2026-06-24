package com.foodtraceability.enterprise.handler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * <p>
 * 统一将 Service 层抛出的 RuntimeException 转换为结构化的 JSON 响应，
 * 避免客户端收到 Spring Boot 默认的 500 错误页面。
 *
 * @author generated
 * @since 2026-06-24
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(RuntimeException.class)
    public Map<String, Object> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("errorCode", "BUSINESS_ERROR");
        result.put("errorMessage", ex.getMessage());
        result.put("timestamp", LocalDateTime.now().format(DATE_FMT));
        return result;
    }

    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleException(Exception ex) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("errorCode", "SYSTEM_ERROR");
        result.put("errorMessage", "系统内部错误: " + ex.getMessage());
        result.put("timestamp", LocalDateTime.now().format(DATE_FMT));
        return result;
    }
}

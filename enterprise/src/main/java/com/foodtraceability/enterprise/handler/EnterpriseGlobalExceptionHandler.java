package com.foodtraceability.enterprise.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@RestControllerAdvice(basePackages = "com.foodtraceability.enterprise.controller")
public class EnterpriseGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(EnterpriseGlobalExceptionHandler.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(RuntimeException.class)
    public Map<String, Object> handleRuntimeException(RuntimeException ex) {
        log.error("业务异常: {}", ex.getClass().getName(), ex);
        Throwable cause = ex.getCause();
        String detail = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        if (cause != null) detail += " | cause: " + cause.getClass().getSimpleName() + " - " + (cause.getMessage() != null ? cause.getMessage() : "(无消息)");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("errorCode", "BUSINESS_ERROR");
        result.put("errorMessage", detail);
        result.put("timestamp", LocalDateTime.now().format(DATE_FMT));
        return result;
    }

    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleException(Exception ex) {
        log.error("系统异常: {}", ex.getClass().getName(), ex);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("errorCode", "SYSTEM_ERROR");
        result.put("errorMessage", ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
        result.put("timestamp", LocalDateTime.now().format(DATE_FMT));
        return result;
    }
}

package com.foodtraceability.enterprise.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Map<String, Object> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("数据完整性异常", ex);
        String msg = ex.getMessage() != null ? ex.getMessage() : "";
        String friendly;
        if (msg.contains("Out of range value")) {
            String col = extractColumn(msg, "column '", "'");
            friendly = "数据保存失败：" + (col != null ? "字段【" + col + "】" : "某个字段") + "的值超出允许范围，请检查输入";
        } else if (msg.contains("cannot be null")) {
            String col = extractColumn(msg, "Column '", "'");
            friendly = "数据保存失败：" + (col != null ? "字段【" + col + "】" : "某个必填字段") + "不能为空";
        } else if (msg.contains("Duplicate entry")) {
            friendly = "数据保存失败：记录已存在，请勿重复提交";
        } else if (msg.contains("Data truncation")) {
            friendly = "数据保存失败：输入的值超出字段允许范围，请调整后重试";
        } else {
            friendly = "数据保存失败：数据不符合数据库约束，请检查输入内容";
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("errorCode", "DATA_ERROR");
        result.put("errorMessage", friendly);
        result.put("timestamp", LocalDateTime.now().format(DATE_FMT));
        return result;
    }

    @ExceptionHandler(RuntimeException.class)
    public Map<String, Object> handleRuntimeException(RuntimeException ex) {
        log.error("业务异常: {}", ex.getClass().getName(), ex);
        String detail = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        // 截断过长的 SQL 异常信息，只返回简要提示
        if (detail != null && detail.length() > 200) {
            detail = detail.substring(0, 200) + "...";
        }
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
        result.put("errorMessage", "系统内部错误，请稍后重试");
        result.put("timestamp", LocalDateTime.now().format(DATE_FMT));
        return result;
    }

    /** 从错误消息中提取列名 */
    private static String extractColumn(String msg, String prefix, String suffix) {
        int s = msg.indexOf(prefix);
        if (s < 0) return null;
        s += prefix.length();
        int e = msg.indexOf(suffix, s);
        if (e < 0) return null;
        return msg.substring(s, e);
    }
}

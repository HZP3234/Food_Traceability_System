package com.foodtraceability.regulation.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /** 仅返回消息，无数据 */
    public static Result<Void> success(String message) {
        return new Result<>(200, message, null);
    }

    public static Result<Void> success() {
        return new Result<>(200, "操作成功", null);
    }

    /** 通用错误，可用于任意返回类型 */
    @SuppressWarnings("unchecked")
    public static <T> Result<T> error(String message) {
        return new Result<>(400, message, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static Result<Void> unauthorized(String message) {
        return new Result<>(401, message, null);
    }

    public static Result<Void> forbidden(String message) {
        return new Result<>(403, message, null);
    }
}

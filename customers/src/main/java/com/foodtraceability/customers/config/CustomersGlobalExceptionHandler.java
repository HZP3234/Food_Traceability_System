package com.foodtraceability.customers.config;

import com.foodtraceability.customers.common.BusinessException;
import com.foodtraceability.customers.dto.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.StringJoiner;

@RestControllerAdvice(basePackages = "com.foodtraceability.customers.controller")
public class CustomersGlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        StringJoiner sj = new StringJoiner("; ");
        e.getBindingResult().getFieldErrors().forEach(error ->
                sj.add(error.getDefaultMessage())
        );
        return Result.fail(400, sj.toString());
    }

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntime(RuntimeException e) {
        return Result.fail(e.getMessage());
    }
}

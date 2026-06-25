package com.foodtraceability.regulation.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtraceability.regulation.entity.AuditLog;
import com.foodtraceability.regulation.security.JwtUtil;
import com.foodtraceability.regulation.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 审计日志 AOP 切面 — 自动拦截所有 Controller 的写操作并写入 t_audit_log
 */
@Aspect
@Component
public class AuditLogAspect {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 切点：enterprise 模块和 regulation 模块的所有 Controller 方法
     * 排除 AuditLogController 自身，避免无限递归
     */
    @Pointcut("(execution(* com.foodtraceability.enterprise.controller..*.*(..)) || " +
              "execution(* com.foodtraceability.regulation.controller..*.*(..))) && " +
              "!execution(* com.foodtraceability.regulation.controller.AuditLogController.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object auditLog(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前 HTTP 请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String httpMethod = request.getMethod();

        // 只记录写操作：POST / PUT / DELETE
        boolean isWrite = "POST".equalsIgnoreCase(httpMethod)
                || "PUT".equalsIgnoreCase(httpMethod)
                || "DELETE".equalsIgnoreCase(httpMethod);
        if (!isWrite) {
            return joinPoint.proceed();
        }

        // 从 JWT Token 中提取操作人信息
        String operatorId = "anonymous";
        String operatorName = "anonymous";
        String token = extractToken(request);
        if (token != null && jwtUtil.validateToken(token)) {
            try {
                operatorId = jwtUtil.getUsername(token);
                operatorName = operatorId;
            } catch (Exception ignored) {}
        }

        // 构建审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setOperatorId(operatorId);
        auditLog.setOperatorName(operatorName);

        // 操作类型
        if ("POST".equalsIgnoreCase(httpMethod)) {
            auditLog.setActionType(1); // 新增
        } else if ("PUT".equalsIgnoreCase(httpMethod)) {
            auditLog.setActionType(2); // 修改
        } else if ("DELETE".equalsIgnoreCase(httpMethod)) {
            auditLog.setActionType(3); // 删除
        }

        // 操作目标描述：类名.方法名 + 请求路径
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String requestUri = request.getRequestURI();
        auditLog.setTargetId(requestUri);
        auditLog.setTargetDesc(className + "." + methodName + " [" + httpMethod + " " + requestUri + "]");

        // 捕获方法参数作为操作数据
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            try {
                // 过滤掉 HttpServletRequest/Response 等非业务参数
                Object[] filteredArgs = java.util.Arrays.stream(args)
                        .filter(a -> !(a instanceof HttpServletRequest)
                                && !(a instanceof jakarta.servlet.http.HttpServletResponse))
                        .toArray();
                if (filteredArgs.length > 0) {
                    auditLog.setAfterData(objectMapper.writeValueAsString(filteredArgs));
                }
            } catch (Exception ignored) {}
        }

        Object result;
        try {
            result = joinPoint.proceed();
            auditLog.setOperationResult(1); // 成功
        } catch (Throwable e) {
            auditLog.setOperationResult(0); // 失败
            auditLog.setFailureReason(truncate(e.getMessage(), 500));
            throw e;
        } finally {
            // 异步写入审计日志，不影响业务
            try {
                auditLogService.writeLog(auditLog);
            } catch (Exception e) {
                System.err.println("[AuditLogAspect] 审计日志写入失败: " + e.getMessage());
            }
        }

        return result;
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }
}

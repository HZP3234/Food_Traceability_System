package com.foodtraceability.regulation.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.foodtraceability.regulation.entity.QualificationFile;
import com.foodtraceability.regulation.mapper.QualificationFileMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * 资质审核拦截器 — 企业用户未提交资质时拦截所有业务请求
 * <p>
 * 白名单路径放行: /api/auth, /api/qualification, /api/upload, Swagger
 * </p>
 */
@Component
@RequiredArgsConstructor
public class QualificationInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final QualificationFileMapper qualificationFileMapper;

    private static final Set<String> ALLOWED_PREFIXES = Set.of(
        "/api/auth",
        "/api/qualification",
        "/api/enterprise",
        "/api/upload",
        "/swagger-ui",
        "/api-docs"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String path = request.getRequestURI();

        // 白名单直接放行
        for (String prefix : ALLOWED_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }

        // 提取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 没有 token → 后续 Security 链会处理
            return true;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return true;
        }

        String roleType = jwtUtil.getRoleType(token);

        // 仅拦截企业用户
        if (!"ENTERPRISE".equals(roleType)) {
            return true;
        }

        String enterpriseUuid = jwtUtil.getEnterpriseUuid(token);
        if (enterpriseUuid == null || enterpriseUuid.isBlank()) {
            return true;
        }

        // 检查是否有审核通过的资质记录（仅审核通过的状态才放行业务操作）
        Long count = qualificationFileMapper.selectCount(
            new LambdaQueryWrapper<QualificationFile>()
                .eq(QualificationFile::getEnterpriseUuid, enterpriseUuid)
                .eq(QualificationFile::getAuditState, 1)
        );

        if (count == null || count == 0) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"code\":403,\"message\":\"请先提交企业资质信息并通过审核后方可使用系统功能\",\"data\":null}"
            );
            return false;
        }

        return true;
    }
}
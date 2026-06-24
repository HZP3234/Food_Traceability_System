package com.foodtraceability.regulation.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT 认证过滤器 — 从请求头提取 Token，校验后设置 SecurityContext
 * <p>
 * 同时将角色信息转为 Spring Security GrantedAuthority，支持 @PreAuthorize 方法级鉴权。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /** 企业类型 → 前端角色名映射 (与 navigation.ts 保持一致) */
    private static final String[] ENTERPRISE_TYPE_ROLE = {
        "",             // 0 — 无
        "SUPPLIER",     // 1 — 供应商
        "MANUFACTURER", // 2 — 加工商
        "LOGISTICS",    // 3 — 物流商
        "SELLER",       // 4 — 零售商
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsername(token);
                String roleType = jwtUtil.getRoleType(token);
                Integer enterpriseType = jwtUtil.getEnterpriseType(token);

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                // 系统角色: ROLE_ADMIN / ROLE_REGULATOR / ROLE_ENTERPRISE
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleType));

                // 企业子角色: ROLE_SUPPLIER / ROLE_MANUFACTURER / ROLE_LOGISTICS / ROLE_SELLER
                if ("ENTERPRISE".equals(roleType) && enterpriseType != null
                        && enterpriseType >= 1 && enterpriseType < ENTERPRISE_TYPE_ROLE.length) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + ENTERPRISE_TYPE_ROLE[enterpriseType]));
                }

                // ADMIN 拥有所有企业角色的权限（便于跨角色查看）
                if ("ADMIN".equals(roleType)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_SUPPLIER"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_MANUFACTURER"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_LOGISTICS"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
                }

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}

package com.foodtraceability.enterprise.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 当前用户工具类 — 从 SecurityContext 获取当前登录用户信息
 */
@Component
public class CurrentUserUtil {

    /**
     * 获取当前登录用户名
     */
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    /**
     * 当前用户是否为生产加工商（纯 MANUFACTURER，不包含 ADMIN）
     * ADMIN 拥有所有企业角色权限（便于跨角色查看），但不应受 create_by 过滤限制
     */
    public boolean isManufacturer() {
        return hasRole("ROLE_MANUFACTURER") && !hasRole("ROLE_ADMIN");
    }

    /**
     * 当前用户是否为管理员
     */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    /**
     * 当前用户是否为销售商（纯 SELLER，不包含 ADMIN）
     */
    public boolean isSeller() {
        return hasRole("ROLE_SELLER") && !hasRole("ROLE_ADMIN");
    }

    /**
     * 当前用户是否为监管机构
     */
    public boolean isRegulator() {
        return hasRole("ROLE_REGULATOR");
    }

    /**
     * 获取当前用户的企业UUID（从JWT中提取）
     */
    public String getEnterpriseUuid() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof String details) {
            return details.isBlank() ? null : details;
        }
        return null;
    }

    /**
     * 当前用户是否为物流商
     */
    public boolean isLogistics() {
        return hasRole("ROLE_LOGISTICS") && !hasRole("ROLE_ADMIN");
    }

    /**
     * 判断当前用户是否拥有指定角色
     */
    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (role.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}

package com.foodtraceability.regulation.service;

import com.foodtraceability.regulation.dto.AuthResponse;
import com.foodtraceability.regulation.dto.LoginRequest;
import com.foodtraceability.regulation.dto.RegisterRequest;
import com.foodtraceability.regulation.entity.SysUser;

/**
 * 系统用户认证服务
 */
public interface SysUserService {

    /**
     * 用户登录，返回 JWT token
     */
    AuthResponse login(LoginRequest request);

    /**
     * 用户注册，返回 JWT token
     */
    AuthResponse register(RegisterRequest request);

    /**
     * 根据用户名查询用户
     */
    SysUser getByUsername(String username);
}

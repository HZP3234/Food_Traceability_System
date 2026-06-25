package com.foodtraceability.regulation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.foodtraceability.regulation.common.Result;
import com.foodtraceability.regulation.dto.AuthResponse;
import com.foodtraceability.regulation.dto.LoginRequest;
import com.foodtraceability.regulation.dto.RegisterRequest;
import com.foodtraceability.regulation.entity.Enterprise;
import com.foodtraceability.regulation.entity.SysUser;
import com.foodtraceability.regulation.mapper.EnterpriseMapper;
import com.foodtraceability.regulation.security.JwtUtil;
import com.foodtraceability.regulation.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统认证 Controller
 */
@Tag(name = "系统认证", description = "登录、注册、获取当前用户信息")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final EnterpriseMapper enterpriseMapper;
    private final JwtUtil jwtUtil;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse resp = sysUserService.login(request);
            return Result.success("登录成功", resp);
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse resp = sysUserService.register(request);
            return Result.success("注册成功", resp);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result<?> me(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            if (!jwtUtil.validateToken(token)) {
                return Result.unauthorized("Token 无效或已过期");
            }
            String username = jwtUtil.getUsername(token);
            SysUser user = sysUserService.getByUsername(username);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            // 查询企业信息
            Integer enterpriseType = null;
            String enterpriseName = "";
            if (user.getEnterpriseUuid() != null && !user.getEnterpriseUuid().isBlank()) {
                Enterprise enterprise = enterpriseMapper.selectOne(
                    new LambdaQueryWrapper<Enterprise>()
                        .eq(Enterprise::getEnterpriseUuid, user.getEnterpriseUuid())
                );
                if (enterprise != null) {
                    enterpriseType = enterprise.getEnterpriseType();
                    enterpriseName = enterprise.getEnterpriseName();
                }
            }
            Map<String, Object> info = new HashMap<>();
            info.put("username", user.getUsername());
            info.put("roleType", user.getRoleType());
            info.put("realName", user.getRealName());
            info.put("enterpriseUuid", user.getEnterpriseUuid() != null ? user.getEnterpriseUuid() : "");
            info.put("enterpriseType", enterpriseType);
            info.put("enterpriseName", enterpriseName);
            info.put("phone", user.getPhone() != null ? user.getPhone() : "");
            info.put("email", user.getEmail() != null ? user.getEmail() : "");
            return Result.success(info);
        } catch (Exception e) {
            return Result.unauthorized("Token 解析失败");
        }
    }
}

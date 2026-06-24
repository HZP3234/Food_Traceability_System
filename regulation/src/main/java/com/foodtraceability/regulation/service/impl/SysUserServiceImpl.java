package com.foodtraceability.regulation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.foodtraceability.regulation.dto.AuthResponse;
import com.foodtraceability.regulation.dto.LoginRequest;
import com.foodtraceability.regulation.dto.RegisterRequest;
import com.foodtraceability.regulation.entity.Enterprise;
import com.foodtraceability.regulation.entity.SysUser;
import com.foodtraceability.regulation.mapper.EnterpriseMapper;
import com.foodtraceability.regulation.mapper.SysUserMapper;
import com.foodtraceability.regulation.security.JwtUtil;
import com.foodtraceability.regulation.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final EnterpriseMapper enterpriseMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AuthResponse login(LoginRequest request) {
        SysUser user = getByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        // 查询企业类型（企业用户才有）
        Integer enterpriseType = null;
        if (user.getEnterpriseUuid() != null && !user.getEnterpriseUuid().isBlank()) {
            Enterprise enterprise = enterpriseMapper.selectOne(
                new LambdaQueryWrapper<Enterprise>()
                    .eq(Enterprise::getEnterpriseUuid, user.getEnterpriseUuid())
            );
            if (enterprise != null) {
                enterpriseType = enterprise.getEnterpriseType();
            }
        }

        // 生成 JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("roleType", user.getRoleType());
        claims.put("realName", user.getRealName());
        claims.put("enterpriseUuid", user.getEnterpriseUuid() != null ? user.getEnterpriseUuid() : "");
        if (enterpriseType != null) {
            claims.put("enterpriseType", enterpriseType);
        }
        String token = jwtUtil.generateToken(user.getUsername(), claims);

        return AuthResponse.builder()
            .token(token)
            .username(user.getUsername())
            .roleType(user.getRoleType())
            .realName(user.getRealName())
            .enterpriseUuid(user.getEnterpriseUuid())
            .enterpriseType(enterpriseType)
            .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        SysUser exist = getByUsername(request.getUsername());
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 企业用户必须关联企业
        if ("ENTERPRISE".equals(request.getRoleType()) && (request.getEnterpriseUuid() == null || request.getEnterpriseUuid().isBlank())) {
            throw new RuntimeException("企业用户必须关联企业");
        }

        SysUser user = new SysUser();
        user.setUserUuid(UUID.randomUUID().toString().replace("-", ""));
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoleType(request.getRoleType());
        user.setEnterpriseUuid(request.getEnterpriseUuid());
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);

        sysUserMapper.insert(user);

        // 查询企业类型（企业用户才有）
        Integer enterpriseType = null;
        if (user.getEnterpriseUuid() != null && !user.getEnterpriseUuid().isBlank()) {
            Enterprise enterprise = enterpriseMapper.selectOne(
                new LambdaQueryWrapper<Enterprise>()
                    .eq(Enterprise::getEnterpriseUuid, user.getEnterpriseUuid())
            );
            if (enterprise != null) {
                enterpriseType = enterprise.getEnterpriseType();
            }
        }

        // 注册成功直接生成 token
        Map<String, Object> claims = new HashMap<>();
        claims.put("roleType", user.getRoleType());
        claims.put("realName", user.getRealName());
        claims.put("enterpriseUuid", user.getEnterpriseUuid() != null ? user.getEnterpriseUuid() : "");
        if (enterpriseType != null) {
            claims.put("enterpriseType", enterpriseType);
        }
        String token = jwtUtil.generateToken(user.getUsername(), claims);

        return AuthResponse.builder()
            .token(token)
            .username(user.getUsername())
            .roleType(user.getRoleType())
            .realName(user.getRealName())
            .enterpriseUuid(user.getEnterpriseUuid())
            .enterpriseType(enterpriseType)
            .build();
    }

    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return sysUserMapper.selectOne(wrapper);
    }
}

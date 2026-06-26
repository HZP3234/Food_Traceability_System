package com.foodtraceability.regulation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.foodtraceability.regulation.dto.AuthResponse;
import com.foodtraceability.regulation.dto.LoginRequest;
import com.foodtraceability.regulation.dto.RegisterRequest;
import com.foodtraceability.regulation.entity.Enterprise;
import com.foodtraceability.regulation.entity.QualificationFile;
import com.foodtraceability.regulation.entity.SysUser;
import com.foodtraceability.regulation.mapper.EnterpriseMapper;
import com.foodtraceability.regulation.mapper.QualificationFileMapper;
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
    private final QualificationFileMapper qualificationFileMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 检查企业用户是否有已提交的资质记录（任意状态，至少提交过） */
    private boolean hasQualification(String enterpriseUuid) {
        if (enterpriseUuid == null || enterpriseUuid.isBlank()) {
            return false;
        }
        Long count = qualificationFileMapper.selectCount(
            new LambdaQueryWrapper<QualificationFile>()
                .eq(QualificationFile::getEnterpriseUuid, enterpriseUuid)
        );
        return count != null && count > 0;
    }

    /** 查询企业信息 */
    private Enterprise getEnterprise(String enterpriseUuid) {
        if (enterpriseUuid == null || enterpriseUuid.isBlank()) {
            return null;
        }
        return enterpriseMapper.selectOne(
            new LambdaQueryWrapper<Enterprise>()
                .eq(Enterprise::getEnterpriseUuid, enterpriseUuid)
        );
    }

    /** 查询企业类型 */
    private Integer resolveEnterpriseType(String enterpriseUuid) {
        Enterprise enterprise = getEnterprise(enterpriseUuid);
        return enterprise != null ? enterprise.getEnterpriseType() : null;
    }

    /** 查询企业名称 */
    private String resolveEnterpriseName(String enterpriseUuid) {
        Enterprise enterprise = getEnterprise(enterpriseUuid);
        return enterprise != null ? enterprise.getEnterpriseName() : null;
    }

    /** 构建 JWT claims */
    private Map<String, Object> buildClaims(SysUser user, Integer enterpriseType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("roleType", user.getRoleType());
        claims.put("realName", user.getRealName());
        claims.put("enterpriseUuid", user.getEnterpriseUuid() != null ? user.getEnterpriseUuid() : "");
        if (enterpriseType != null) {
            claims.put("enterpriseType", enterpriseType);
        }
        return claims;
    }

    /** 构建 AuthResponse */
    private AuthResponse buildAuthResponse(SysUser user, Integer enterpriseType) {
        String enterpriseUuid = user.getEnterpriseUuid() != null ? user.getEnterpriseUuid() : "";
        return AuthResponse.builder()
            .token(jwtUtil.generateToken(user.getUsername(), buildClaims(user, enterpriseType)))
            .username(user.getUsername())
            .roleType(user.getRoleType())
            .realName(user.getRealName())
            .enterpriseUuid(enterpriseUuid)
            .enterpriseType(enterpriseType)
            .enterpriseName(resolveEnterpriseName(enterpriseUuid))
            .hasQualification(hasQualification(enterpriseUuid))
            .build();
    }

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

        Integer enterpriseType = resolveEnterpriseType(user.getEnterpriseUuid());
        return buildAuthResponse(user, enterpriseType);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        SysUser exist = getByUsername(request.getUsername());
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }

        Integer enterpriseType = null;
        String enterpriseUuid = null;

        // 企业用户：自动创建 Enterprise 记录
        if ("ENTERPRISE".equals(request.getRoleType())) {
            if (request.getEnterpriseName() == null || request.getEnterpriseName().isBlank()) {
                throw new RuntimeException("企业用户必须填写企业名称");
            }
            if (request.getEnterpriseType() == null || request.getEnterpriseType() < 1 || request.getEnterpriseType() > 4) {
                throw new RuntimeException("企业用户必须选择企业类型");
            }

            enterpriseType = request.getEnterpriseType();
            enterpriseUuid = UUID.randomUUID().toString().replace("-", "");

            Enterprise enterprise = new Enterprise();
            enterprise.setEnterpriseUuid(enterpriseUuid);
            enterprise.setEnterpriseName(request.getEnterpriseName());
            enterprise.setEnterpriseType(enterpriseType);
            enterprise.setCertNo(request.getCertNo() != null ? request.getCertNo() : "");
            enterprise.setContactPhone(request.getContactPhone() != null ? request.getContactPhone() : "");
            enterprise.setContactPerson(request.getContactPerson() != null ? request.getContactPerson() : "");
            enterprise.setAddress(request.getAddress() != null ? request.getAddress() : "");
            enterprise.setStatus(1);
            enterprise.setRiskLevel(1); // 默认低风险
            enterprise.setCreateBy(request.getUsername());
            enterprise.setUpdateBy(request.getUsername());
            enterprise.setCreateTime(LocalDateTime.now());
            enterprise.setUpdateTime(LocalDateTime.now());
            enterpriseMapper.insert(enterprise);
        }

        SysUser user = new SysUser();
        user.setUserUuid(UUID.randomUUID().toString().replace("-", ""));
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoleType(request.getRoleType());
        user.setEnterpriseUuid(enterpriseUuid);
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        user.setCreateBy(request.getUsername());
        user.setUpdateBy(request.getUsername());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        sysUserMapper.insert(user);

        return buildAuthResponse(user, enterpriseType);
    }

    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return sysUserMapper.selectOne(wrapper);
    }
}
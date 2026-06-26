package com.foodtraceability.regulation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.foodtraceability.regulation.common.Result;
import com.foodtraceability.regulation.entity.QualificationFile;
import com.foodtraceability.regulation.mapper.QualificationFileMapper;
import com.foodtraceability.regulation.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 企业资质管理 Controller
 * <p>
 * - 企业用户：提交/查看本企业资质
 * - 监管机构：审核资质（通过/拒绝）+ 查看所有资质
 * </p>
 */
@Tag(name = "企业资质管理", description = "资质提交、查看、审核")
@RestController
@RequestMapping("/api/qualification")
@RequiredArgsConstructor
public class QualificationController {

    private final QualificationFileMapper qualificationFileMapper;
    private final JwtUtil jwtUtil;

    /** 从 Token 中获取 enterpriseUuid */
    private String getEnterpriseUuid(String token) {
        return jwtUtil.getEnterpriseUuid(token);
    }

    /** 从请求头提取 Token */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /** 获取当前用户角色类型 */
    private String getRoleType(String token) {
        return jwtUtil.getRoleType(token);
    }

    // ==================== 企业用户 ====================

    @Operation(summary = "提交资质")
    @PostMapping("/submit")
    public Result<?> submit(@RequestBody QualificationFile qualification,
                            HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) return Result.error(401, "未登录");

        String enterpriseUuid = getEnterpriseUuid(token);
        if (enterpriseUuid == null || enterpriseUuid.isBlank()) {
            return Result.error(400, "当前用户未关联企业");
        }

        qualification.setEnterpriseUuid(enterpriseUuid);
        qualification.setQualificationUuid(UUID.randomUUID().toString().replace("-", ""));
        if (qualification.getQualificationState() == null) {
            qualification.setQualificationState(1);
        }
        qualification.setAuditState(0); // 待审核
        if (qualification.getFileHash() == null) {
            qualification.setFileHash("");
        }
        qualification.setCreateTime(LocalDateTime.now());
        qualification.setUpdateTime(LocalDateTime.now());
        qualification.setCreateBy(getEnterpriseUuid(token));
        qualification.setUpdateBy(getEnterpriseUuid(token));
        qualificationFileMapper.insert(qualification);
        return Result.success("资质提交成功，等待监管审核", qualification);
    }

    @Operation(summary = "更新资质")
    @PutMapping("/{qualificationId}")
    public Result<?> update(@PathVariable Long qualificationId,
                            @RequestBody QualificationFile qualification,
                            HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) return Result.error(401, "未登录");

        String enterpriseUuid = getEnterpriseUuid(token);
        QualificationFile existing = qualificationFileMapper.selectById(qualificationId);
        if (existing == null) {
            return Result.error(404, "资质记录不存在");
        }
        if (!existing.getEnterpriseUuid().equals(enterpriseUuid)) {
            return Result.error(403, "无权修改其他企业的资质");
        }

        qualification.setQualificationId(qualificationId);
        qualification.setEnterpriseUuid(enterpriseUuid);
        qualification.setAuditState(0); // 修改后重新待审核
        if (qualification.getFileHash() == null) {
            qualification.setFileHash("");
        }
        qualification.setUpdateTime(LocalDateTime.now());
        qualification.setUpdateBy(enterpriseUuid);
        qualificationFileMapper.updateById(qualification);
        return Result.success("资质更新成功，已重新提交审核", qualification);
    }

    @Operation(summary = "查看本企业的资质列表")
    @GetMapping("/my")
    public Result<?> my(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) return Result.error(401, "未登录");

        String enterpriseUuid = getEnterpriseUuid(token);
        if (enterpriseUuid == null || enterpriseUuid.isBlank()) {
            return Result.error(400, "当前用户未关联企业");
        }

        List<QualificationFile> list = qualificationFileMapper.selectByEnterpriseUuid(enterpriseUuid);
        return Result.success(list);
    }

    @Operation(summary = "查看资质审核状态")
    @GetMapping("/status")
    public Result<?> status(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) return Result.error(401, "未登录");

        String enterpriseUuid = getEnterpriseUuid(token);
        if (enterpriseUuid == null || enterpriseUuid.isBlank()) {
            return Result.error(400, "当前用户未关联企业");
        }

        List<QualificationFile> list = qualificationFileMapper.selectByEnterpriseUuid(enterpriseUuid);
        long approved = list.stream().filter(q -> q.getAuditState() != null && q.getAuditState() == 1).count();
        long pending = list.stream().filter(q -> q.getAuditState() != null && q.getAuditState() == 0).count();
        long rejected = list.stream().filter(q -> q.getAuditState() != null && q.getAuditState() == 2).count();

        return Result.success(Map.of(
            "enterpriseUuid", enterpriseUuid,
            "hasQualification", !list.isEmpty(),
            "totalCount", list.size(),
            "approvedCount", approved,
            "pendingCount", pending,
            "rejectedCount", rejected
        ));
    }

    // ==================== 监管机构 ====================

    @Operation(summary = "查看所有企业的资质列表（监管用）")
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public Result<?> all(@RequestParam(required = false) Integer auditState,
                         @RequestParam(required = false) String enterpriseUuid) {
        LambdaQueryWrapper<QualificationFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(auditState != null, QualificationFile::getAuditState, auditState)
               .eq(enterpriseUuid != null && !enterpriseUuid.isBlank(),
                   QualificationFile::getEnterpriseUuid, enterpriseUuid)
               .orderByDesc(QualificationFile::getCreateTime);
        return Result.success(qualificationFileMapper.selectList(wrapper));
    }

    @Operation(summary = "审核通过")
    @PutMapping("/{qualificationId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public Result<?> approve(@PathVariable Long qualificationId,
                             @RequestBody(required = false) Map<String, String> body,
                             HttpServletRequest request) {
        QualificationFile q = qualificationFileMapper.selectById(qualificationId);
        if (q == null) return Result.error(404, "资质记录不存在");

        q.setAuditState(1); // 审核通过
        if (body != null && body.containsKey("remark")) {
            q.setAuditRemark(body.get("remark"));
        }
        q.setUpdateTime(LocalDateTime.now());
        q.setUpdateBy(getRoleType(extractToken(request)));
        qualificationFileMapper.updateById(q);
        return Result.success("审核通过", q);
    }

    @Operation(summary = "审核拒绝")
    @PutMapping("/{qualificationId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public Result<?> reject(@PathVariable Long qualificationId,
                            @RequestBody(required = false) Map<String, String> body,
                            HttpServletRequest request) {
        QualificationFile q = qualificationFileMapper.selectById(qualificationId);
        if (q == null) return Result.error(404, "资质记录不存在");

        q.setAuditState(2); // 审核不通过
        if (body != null && body.containsKey("remark")) {
            q.setAuditRemark(body.get("remark"));
        }
        q.setUpdateTime(LocalDateTime.now());
        q.setUpdateBy(getRoleType(extractToken(request)));
        qualificationFileMapper.updateById(q);
        return Result.success("已拒绝", q);
    }
}
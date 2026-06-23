package com.foodtraceability.regulation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodtraceability.regulation.common.Result;
import com.foodtraceability.regulation.entity.Enterprise;
import com.foodtraceability.regulation.service.EnterpriseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业资质管理 Controller (需求 3.2.9)
 */
@Tag(name = "企业资质管理", description = "企业基本信息与资质审核管理")
@RestController
@RequestMapping("/api/enterprise")
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    @Operation(summary = "查询企业列表")
    @GetMapping
    public Result<Page<Enterprise>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String enterpriseName,
            @RequestParam(required = false) Integer enterpriseType) {
        Page<Enterprise> p = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Enterprise> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.like(enterpriseName != null, Enterprise::getEnterpriseName, enterpriseName)
               .eq(enterpriseType != null, Enterprise::getEnterpriseType, enterpriseType)
               .orderByDesc(Enterprise::getCreateTime);
        return Result.success(enterpriseService.page(p, wrapper));
    }

    @Operation(summary = "查看企业详情")
    @GetMapping("/{enterpriseId}")
    public Result<Enterprise> detail(@PathVariable Long enterpriseId) {
        return Result.success(enterpriseService.getById(enterpriseId));
    }

    @Operation(summary = "按风险等级筛选")
    @GetMapping("/risk/{level}")
    public Result<List<Enterprise>> listByRisk(@PathVariable Integer level) {
        return Result.success(enterpriseService.listByRiskLevel(level));
    }

    @Operation(summary = "新增企业")
    @PostMapping
    public Result<Enterprise> create(@RequestBody Enterprise enterprise) {
        enterpriseService.save(enterprise);
        return Result.success(enterprise);
    }

    @Operation(summary = "修改企业信息")
    @PutMapping("/{enterpriseId}")
    public Result<Enterprise> update(@PathVariable Long enterpriseId, @RequestBody Enterprise enterprise) {
        enterprise.setEnterpriseId(enterpriseId);
        enterpriseService.updateById(enterprise);
        return Result.success(enterprise);
    }

    @Operation(summary = "删除企业（逻辑删除）")
    @DeleteMapping("/{enterpriseId}")
    public Result<Void> delete(@PathVariable Long enterpriseId) {
        enterpriseService.removeById(enterpriseId);
        return Result.success();
    }

    @Operation(summary = "手动触发资质状态检查")
    @PostMapping("/check-status")
    public Result<Void> checkStatus() {
        enterpriseService.checkAndUpdateQualificationStatus();
        return Result.success("资质状态检查已触发");
    }
}

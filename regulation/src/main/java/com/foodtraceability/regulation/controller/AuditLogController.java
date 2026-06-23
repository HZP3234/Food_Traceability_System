package com.foodtraceability.regulation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodtraceability.regulation.common.Result;
import com.foodtraceability.regulation.entity.AuditLog;
import com.foodtraceability.regulation.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 操作日志审计 Controller (需求 3.2.11)
 */
@Tag(name = "操作日志审计", description = "操作日志记录、查询、归档与完整性校验")
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Operation(summary = "审计日志分页查询")
    @GetMapping
    public Result<Page<AuditLog>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String operatorName,
            @RequestParam(required = false) Integer actionType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Page<AuditLog> p = new Page<>(page, size);
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(operatorName != null, AuditLog::getOperatorName, operatorName)
               .eq(actionType != null, AuditLog::getActionType, actionType)
               .ge(startTime != null, AuditLog::getOperationTime, startTime)
               .le(endTime != null, AuditLog::getOperationTime, endTime)
               .orderByDesc(AuditLog::getOperationTime);
        return Result.success(auditLogService.page(p, wrapper));
    }

    @Operation(summary = "查看日志详情（含前后数据对比）")
    @GetMapping("/{logId}")
    public Result<AuditLog> detail(@PathVariable Long logId) {
        return Result.success(auditLogService.getById(logId));
    }

    @Operation(summary = "手工写入审计日志")
    @PostMapping
    public Result<AuditLog> write(@RequestBody AuditLog log) {
        return Result.success(auditLogService.writeLog(log));
    }

    @Operation(summary = "校验日志Hash链完整性")
    @GetMapping("/verify-chain")
    public Result<Map<String, Object>> verifyChain() {
        boolean valid = auditLogService.verifyLogChainIntegrity();
        return Result.success(Map.of(
            "chainValid", valid,
            "message", valid ? "日志Hash链完整性校验通过" : "日志Hash链存在断裂，可能有篡改！"
        ));
    }

    @Operation(summary = "手动触发日志归档（12个月以前）")
    @PostMapping("/archive")
    public Result<Map<String, Object>> archive() {
        int count = auditLogService.archiveOldLogs();
        return Result.success(Map.of(
            "archivedCount", count,
            "message", "已归档 " + count + " 条日志"
        ));
    }
}

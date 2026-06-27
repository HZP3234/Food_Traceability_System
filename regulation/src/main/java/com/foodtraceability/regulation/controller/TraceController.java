package com.foodtraceability.regulation.controller;

import com.foodtraceability.regulation.common.Result;
import com.foodtraceability.regulation.entity.TraceCode;
import com.foodtraceability.regulation.service.TraceCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 监管全链追溯 Controller (需求 3.2.10)
 */
@Tag(name = "监管全链追溯", description = "溯源码管理与全链路查询校验")
@RestController
@RequestMapping("/api/trace")
@RequiredArgsConstructor
public class TraceController {

    private final TraceCodeService traceCodeService;

    @Operation(summary = "按溯源码精确查询")
    @GetMapping("/code/{traceCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR', 'MANUFACTURER')")
    public Result<TraceCode> getByCode(@PathVariable String traceCode) {
        TraceCode tc = traceCodeService.getByTraceCode(traceCode);
        if (tc == null) {
            return Result.error("溯源码不存在: " + traceCode);
        }
        return Result.success(tc);
    }

    @Operation(summary = "按批次号查询溯源码列表")
    @GetMapping("/batch/{batchNo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR', 'MANUFACTURER')")
    public Result<List<TraceCode>> listByBatch(@PathVariable String batchNo) {
        return Result.success(traceCodeService.listByBatchNo(batchNo));
    }

    @Operation(summary = "按企业查询溯源码")
    @GetMapping("/enterprise/{enterpriseUuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public Result<List<TraceCode>> listByEnterprise(@PathVariable String enterpriseUuid) {
        return Result.success(traceCodeService.listByEnterprise(enterpriseUuid));
    }

    @Operation(summary = "校验溯源码内容Hash完整性")
    @GetMapping("/verify/{traceCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public Result<Map<String, Object>> verifyHash(@PathVariable String traceCode) {
        boolean valid = traceCodeService.verifyContentHash(traceCode);
        return Result.success(Map.of(
            "traceCode", traceCode,
            "hashValid", valid,
            "message", valid ? "Hash校验通过" : "Hash不一致，数据可能被篡改！"
        ));
    }

    @Operation(summary = "禁用溯源码")
    @PutMapping("/disable/{traceCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public Result<Void> disable(@PathVariable String traceCode, @RequestParam String reason) {
        traceCodeService.disableTraceCode(traceCode, reason);
        return Result.success("溯源码已禁用");
    }

    @Operation(summary = "作废溯源码")
    @PutMapping("/void/{traceCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public Result<Void> voidCode(@PathVariable String traceCode, @RequestParam String reason) {
        traceCodeService.voidTraceCode(traceCode, reason);
        return Result.success("溯源码已作废");
    }

    @Operation(summary = "恢复溯源码（从禁用/作废恢复为正常）")
    @PutMapping("/restore/{traceCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public Result<Void> restore(@PathVariable String traceCode) {
        traceCodeService.restoreTraceCode(traceCode);
        return Result.success("溯源码已恢复正常");
    }

    @Operation(summary = "查询所有溯源码")
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR', 'MANUFACTURER')")
    public Result<List<TraceCode>> listAll() {
        return Result.success(traceCodeService.listAll());
    }

    @Operation(summary = "按企业名称模糊查询溯源码")
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR', 'MANUFACTURER')")
    public Result<List<TraceCode>> searchByEnterpriseName(@RequestParam String name) {
        return Result.success(traceCodeService.listByEnterpriseName(name));
    }
}

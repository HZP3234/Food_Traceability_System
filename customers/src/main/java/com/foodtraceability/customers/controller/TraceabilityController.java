package com.foodtraceability.customers.controller;

import com.foodtraceability.customers.dto.Result;
import com.foodtraceability.customers.dto.ScanRecordDTO;
import com.foodtraceability.customers.dto.TraceabilityQueryDTO;
import com.foodtraceability.customers.dto.TraceabilityVO;
import com.foodtraceability.customers.entity.ScanRecord;
import com.foodtraceability.customers.service.TraceabilityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traceability")
@RequiredArgsConstructor
public class TraceabilityController {

    private final TraceabilityService traceabilityService;

    @PostMapping("/query")
    public Result<TraceabilityVO> query(@Valid @RequestBody TraceabilityQueryDTO dto,
                                        HttpServletRequest request) {
        TraceabilityVO vo = traceabilityService.queryByBatchNo(dto.getProductBatchNo());
        traceabilityService.recordScan(dto.getProductBatchNo(), getClientIp(request), null, dto.getUserId());
        return Result.success(vo);
    }

    @GetMapping("/query-by-code")
    public Result<TraceabilityVO> queryByCode(@RequestParam String traceCode,
                                              @RequestParam(required = false) String userId,
                                              HttpServletRequest request) {
        TraceabilityVO vo = traceabilityService.queryByTraceCode(traceCode);
        traceabilityService.recordScan(vo.getProductBatchNo(), getClientIp(request), null, userId);
        return Result.success(vo);
    }

    @PostMapping("/scan")
    public Result<Void> scan(@Valid @RequestBody ScanRecordDTO dto,
                             HttpServletRequest request) {
        traceabilityService.recordScan(dto.getProductBatchNo(),
                dto.getScanIp() != null ? dto.getScanIp() : getClientIp(request),
                dto.getScanLocation(),
                dto.getUserId());
        return Result.success();
    }

    @GetMapping("/scans")
    public Result<List<ScanRecord>> recentScans(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(traceabilityService.getRecentScans(limit));
    }

    @GetMapping("/user-scans")
    public Result<List<ScanRecord>> userScans(@RequestParam String userId,
                                               @RequestParam(defaultValue = "50") int limit) {
        return Result.success(traceabilityService.getScansByUserId(userId, limit));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

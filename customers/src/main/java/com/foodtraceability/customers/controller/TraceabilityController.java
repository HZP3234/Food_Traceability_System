package com.foodtraceability.customers.controller;

import com.foodtraceability.customers.dto.Result;
import com.foodtraceability.customers.dto.ScanRecordDTO;
import com.foodtraceability.customers.dto.TraceabilityQueryDTO;
import com.foodtraceability.customers.dto.TraceabilityVO;
import com.foodtraceability.customers.service.TraceabilityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/traceability")
@RequiredArgsConstructor
public class TraceabilityController {

    private final TraceabilityService traceabilityService;

    @PostMapping("/query")
    public Result<TraceabilityVO> query(@Valid @RequestBody TraceabilityQueryDTO dto,
                                        HttpServletRequest request) {
        TraceabilityVO vo = traceabilityService.queryByBatchNo(dto.getProductBatchNo());
        safeRecordScan(vo.getProductBatchNo(), getClientIp(request), dto.getUserId(), dto.getTraceCode());
        return Result.success(vo);
    }

    @GetMapping("/query-by-code")
    public Result<TraceabilityVO> queryByCode(@RequestParam String traceCode,
                                              @RequestParam(required = false) String userId,
                                              HttpServletRequest request) {
        TraceabilityVO vo = traceabilityService.queryByTraceCode(traceCode);
        safeRecordScan(vo.getProductBatchNo(), getClientIp(request), userId, traceCode);
        return Result.success(vo);
    }

    @PostMapping("/scan")
    public Result<Void> scan(@Valid @RequestBody ScanRecordDTO dto,
                             HttpServletRequest request) {
        traceabilityService.recordScan(dto.getProductBatchNo(),
                dto.getScanIp() != null ? dto.getScanIp() : getClientIp(request),
                dto.getScanLocation(),
                dto.getUserId(),
                dto.getTraceCode());
        return Result.success();
    }

    private void safeRecordScan(String productBatchNo, String clientIp, String userId, String traceCode) {
        try {
            traceabilityService.recordScan(productBatchNo, clientIp, null, userId, traceCode);
        } catch (Exception e) {
            log.error("记录扫码失败: productBatchNo={}, traceCode={}", productBatchNo, traceCode, e);
        }
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

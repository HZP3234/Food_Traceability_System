package com.foodtraceability.regulation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodtraceability.regulation.common.Result;
import com.foodtraceability.regulation.dto.ComplaintHandleDTO;
import com.foodtraceability.regulation.dto.ComplaintPageDTO;
import com.foodtraceability.regulation.dto.ComplaintStats;
import com.foodtraceability.regulation.entity.ComplaintHandleLog;
import com.foodtraceability.regulation.entity.ComplaintRecord;
import com.foodtraceability.regulation.security.JwtUtil;
import com.foodtraceability.regulation.service.RegulationComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 投诉处理 Controller (监管端)
 * <p>
 * 监管机构（ADMIN / REGULATOR）对消费者投诉进行全流程管理：
 * 查看列表 → 查看详情 → 受理 → 调查 → 回复 → 办结 / 驳回
 * </p>
 */
@Tag(name = "投诉处理管理", description = "监管端投诉受理、处理、关闭、统计")
@RestController
@RequestMapping("/api/regulation/complaint")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
public class RegulationComplaintController {

    private final RegulationComplaintService complaintService;
    private final JwtUtil jwtUtil;

    /** 从请求头提取 Token */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /** 从 Token 获取处理人姓名 */
    private String getHandlerName(String token) {
        return jwtUtil.getRealName(token);
    }

    /** 从 Token 获取操作人ID */
    private Long getOperatorId(String token) {
        return jwtUtil.getUserId(token);
    }

    // ==================== 查询 ====================

    @Operation(summary = "分页查询投诉列表")
    @PostMapping("/page")
    public Result<?> page(@RequestBody ComplaintPageDTO dto) {
        Page<ComplaintRecord> page = complaintService.pageQuery(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        return Result.success(result);
    }

    @Operation(summary = "获取投诉详情（含处理日志）")
    @GetMapping("/detail/{complaintRecordId}")
    public Result<?> detail(@PathVariable Long complaintRecordId) {
        ComplaintRecord record = complaintService.detail(complaintRecordId);
        if (record == null) {
            return Result.error(404, "投诉记录不存在");
        }
        List<ComplaintHandleLog> logs = complaintService.getHandleLogs(record.getComplaintNo());
        Map<String, Object> result = new HashMap<>();
        result.put("complaint", record);
        result.put("handleLogs", logs);
        return Result.success(result);
    }

    @Operation(summary = "获取投诉处理日志")
    @GetMapping("/logs/{complaintNo}")
    public Result<?> logs(@PathVariable String complaintNo) {
        List<ComplaintHandleLog> logs = complaintService.getHandleLogs(complaintNo);
        return Result.success(logs);
    }

    @Operation(summary = "投诉统计概览")
    @GetMapping("/stats")
    public Result<ComplaintStats> stats() {
        return Result.success(complaintService.getStats());
    }

    // ==================== 处理操作 ====================

    @Operation(summary = "执行投诉处理操作")
    @PostMapping("/handle")
    public Result<?> handle(@RequestBody ComplaintHandleDTO dto,
                            HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return Result.unauthorized("未登录");
        }
        String handlerName = getHandlerName(token);
        if (handlerName == null || handlerName.isBlank()) {
            handlerName = "监管人员";
        }

        try {
            Long operatorId = getOperatorId(token);
            ComplaintRecord record = complaintService.handle(dto, handlerName, operatorId);
            Map<String, Object> data = new HashMap<>();
            data.put("complaint", record);
            data.put("message", buildActionResult(dto.getAction()));
            return Result.success(buildActionResult(dto.getAction()), data);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 快捷操作 ====================

    @Operation(summary = "受理投诉（快捷操作）")
    @PutMapping("/{complaintRecordId}/accept")
    public Result<?> accept(@PathVariable Long complaintRecordId,
                            @RequestBody(required = false) Map<String, String> body,
                            HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) return Result.unauthorized("未登录");

        ComplaintHandleDTO dto = new ComplaintHandleDTO();
        dto.setComplaintRecordId(complaintRecordId);
        dto.setAction("ACCEPT");
        if (body != null) {
            dto.setRemark(body.get("remark"));
            if (body.containsKey("priority")) {
                try {
                    dto.setPriority(Integer.parseInt(body.get("priority")));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        try {
            ComplaintRecord record = complaintService.handle(dto, getHandlerName(token), getOperatorId(token));
            return Result.success("投诉已受理，处理人: " + record.getHandlerName(), record);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "办结投诉（快捷操作）")
    @PutMapping("/{complaintRecordId}/close")
    public Result<?> close(@PathVariable Long complaintRecordId,
                           @RequestBody Map<String, String> body,
                           HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) return Result.unauthorized("未登录");

        ComplaintHandleDTO dto = new ComplaintHandleDTO();
        dto.setComplaintRecordId(complaintRecordId);
        dto.setAction("CLOSE");
        if (body != null) {
            dto.setRemark(body.get("remark"));
            dto.setHandlingConclusion(body.get("handlingConclusion"));
            dto.setHandlingProofUrls(body.get("handlingProofUrls"));
        }

        try {
            ComplaintRecord record = complaintService.handle(dto, getHandlerName(token), getOperatorId(token));
            return Result.success("投诉已办结", record);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "驳回投诉（快捷操作）")
    @PutMapping("/{complaintRecordId}/reject")
    public Result<?> reject(@PathVariable Long complaintRecordId,
                            @RequestBody(required = false) Map<String, String> body,
                            HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) return Result.unauthorized("未登录");

        ComplaintHandleDTO dto = new ComplaintHandleDTO();
        dto.setComplaintRecordId(complaintRecordId);
        dto.setAction("REJECT");
        if (body != null) {
            dto.setRemark(body.get("remark"));
            dto.setHandlingConclusion(body.get("handlingConclusion"));
        }

        try {
            ComplaintRecord record = complaintService.handle(dto, getHandlerName(token), getOperatorId(token));
            return Result.success("投诉已驳回", record);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== helper ====================

    private String buildActionResult(String action) {
        if (action == null) return "操作完成";
        return switch (action.toUpperCase()) {
            case "ACCEPT" -> "投诉已受理，开始处理";
            case "INVESTIGATE" -> "调查记录已保存";
            case "RESPOND" -> "回复已记录";
            case "CLOSE" -> "投诉已办结关闭";
            case "REJECT" -> "投诉已驳回";
            default -> "操作完成";
        };
    }
}

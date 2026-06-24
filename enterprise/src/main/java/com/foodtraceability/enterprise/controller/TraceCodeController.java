package com.foodtraceability.enterprise.controller;

import com.foodtraceability.enterprise.dto.*;
import com.foodtraceability.enterprise.entity.TraceCode;
import com.foodtraceability.enterprise.entity.TraceCodeBind;
import com.foodtraceability.enterprise.service.TraceCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 溯源码管理 Controller
 * <p>
 * 提供溯源码全生命周期管理 REST API：
 * 生成（单条/批量）、绑定、查询、状态变更、扫码校验、标签导出。
 * <p>
 * 功能对应概要设计说明书 4.2.1.9 溯源码管理模块 + 表格 65~74。
 *
 * @author GuangYao Liu
 * @since 2026-06-23
 */
@RestController
@RequestMapping("/TraceCode")
public class TraceCodeController {

    @Autowired
    private TraceCodeService traceCodeService;

    // ==================== 溯源码生成 ====================

    /**
     * 生成单条溯源码并与业务数据绑定
     * <p>
     * 对应概要设计 表格65：generateTraceCode
     */
    @RequestMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public TraceCodeVO generateTraceCode(TraceCodeGenerateDTO dto) {
        return traceCodeService.generateTraceCode(dto);
    }

    /**
     * 按产品批次批量生成溯源码
     * <p>
     * 对应概要设计 表格66：batchGenerateTraceCode
     */
    @RequestMapping("/batchGenerate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public List<TraceCodeVO> batchGenerateTraceCode(TraceCodeBatchDTO dto) {
        return traceCodeService.batchGenerateTraceCode(dto);
    }

    // ==================== 查询 ====================

    /**
     * 按溯源码值精确查询
     */
    @RequestMapping("/queryByCode")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR', 'SELLER', 'LOGISTICS', 'SUPPLIER')")
    public TraceCode queryByCode(@RequestParam String traceCode) {
        return traceCodeService.getByCode(traceCode);
    }

    /**
     * 查询溯源码完整详情（含绑定关系）
     * <p>
     * 对应概要设计 表格67：queryTraceCodeDetail
     */
    @RequestMapping("/queryDetail")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR', 'SELLER', 'LOGISTICS', 'SUPPLIER')")
    public TraceCodeDetailVO queryDetail(@RequestParam String traceCode) {
        return traceCodeService.queryTraceCodeDetail(traceCode);
    }

    /**
     * 多条件分页查询溯源码列表
     * <p>
     * 对应概要设计 表格69：queryTraceCodeList
     */
    @RequestMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public List<TraceCodeVO> listTraceCode(TraceCodeQueryDTO dto) {
        return traceCodeService.queryTraceCodeList(dto);
    }

    /**
     * 按批次号查询所有溯源码
     */
    @RequestMapping("/listByBatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public List<TraceCode> listByBatch(@RequestParam String batchNo) {
        return traceCodeService.listByBatchNo(batchNo);
    }

    /**
     * 按企业ID查询溯源码
     */
    @RequestMapping("/listByEnterprise")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public List<TraceCode> listByEnterprise(@RequestParam String enterpriseId) {
        return traceCodeService.listByEnterprise(enterpriseId);
    }

    /**
     * 按生成批次号查询（同一批批量生成的溯源码）
     */
    @RequestMapping("/listByGenerateBatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public List<TraceCode> listByGenerateBatch(@RequestParam String generateBatchNo) {
        return traceCodeService.listByGenerateBatchNo(generateBatchNo);
    }

    // ==================== 消费者扫码 ====================

    /**
     * 消费者扫码查询接口（公开接口 — 在 SecurityConfig 中已放行 /api/consumer/**，此处用于内部调用）
     * <p>
     * 对应概要设计 表格70：queryByCode
     */
    @RequestMapping("/scan")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public ConsumerTraceVO scan(@RequestParam String traceCode) {
        return traceCodeService.queryByCode(traceCode);
    }

    // ==================== 状态变更 ====================

    /**
     * 变更溯源码状态（启用/禁用/作废）
     * <p>
     * 对应概要设计 表格68：updateTraceCodeStatus
     */
    @RequestMapping("/updateStatus")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public String updateStatus(TraceCodeStatusDTO dto) {
        traceCodeService.updateTraceCodeStatus(dto);
        String statusName = TraceCodeService.getStatusName(dto.getTargetStatus());
        return "溯源码 " + dto.getTraceCode() + " 已更新为" + statusName;
    }

    /**
     * 禁用溯源码（快捷接口）
     */
    @RequestMapping("/disable")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public String disable(@RequestParam String traceCode,
                           @RequestParam String reason,
                           @RequestParam String operator) {
        TraceCodeStatusDTO dto = new TraceCodeStatusDTO();
        dto.setTraceCode(traceCode);
        dto.setTargetStatus(3); // 已禁用
        dto.setReason(reason);
        dto.setOperator(operator);
        traceCodeService.updateTraceCodeStatus(dto);
        return "溯源码已禁用: " + traceCode;
    }

    /**
     * 作废溯源码（快捷接口）
     */
    @RequestMapping("/void")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public String voidCode(@RequestParam String traceCode,
                            @RequestParam String reason,
                            @RequestParam String operator) {
        TraceCodeStatusDTO dto = new TraceCodeStatusDTO();
        dto.setTraceCode(traceCode);
        dto.setTargetStatus(4); // 已作废
        dto.setReason(reason);
        dto.setOperator(operator);
        traceCodeService.updateTraceCodeStatus(dto);
        return "溯源码已作废: " + traceCode;
    }

    // ==================== 校验 ====================

    /**
     * 溯源码校验接口（供监管全链追溯模块内部调用）
     * <p>
     * 对应概要设计 表格72：verifyTraceCode
     */
    @RequestMapping("/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public TraceCode verify(@RequestParam String traceCode) {
        return traceCodeService.verifyTraceCode(traceCode);
    }

    /**
     * 校验溯源码内容Hash完整性
     */
    @RequestMapping("/verifyHash")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public String verifyHash(@RequestParam String traceCode) {
        TraceCode tc = traceCodeService.getByCode(traceCode);
        if (tc == null) {
            return "溯源码不存在: " + traceCode;
        }
        String recalculated = traceCodeService.computeContentHash(tc);
        boolean valid = recalculated != null && recalculated.equals(tc.getContentHash());
        return "溯源码: " + traceCode + ", Hash校验: " + (valid ? "通过 ✓" : "不一致 ✗ 数据可能被篡改！");
    }

    // ==================== 绑定关系 ====================

    /**
     * 建立溯源码与业务数据的绑定关系
     */
    @RequestMapping("/bindBiz")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String bindBiz(@RequestParam String traceCode,
                           @RequestParam String bizType,
                           @RequestParam String bizId,
                           @RequestParam String bizNo,
                           @RequestParam String operator) {
        traceCodeService.bindBusinessData(traceCode, bizType, bizId, bizNo, operator);
        return "绑定成功: " + traceCode + " -> " + bizType + "(" + bizNo + ")";
    }

    /**
     * 查询溯源码的所有绑定关系
     */
    @RequestMapping("/listBinds")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public List<TraceCodeBind> listBinds(@RequestParam String traceCode) {
        return traceCodeService.listBindsByCode(traceCode);
    }

    // ==================== 导出 ====================

    /**
     * 导出溯源码二维码/条形码标签文件
     * <p>
     * 对应概要设计 表格74：exportTraceCodeLabel
     */
    @RequestMapping("/exportLabel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String exportLabel(TraceCodeExportDTO dto) {
        String exportId = traceCodeService.exportTraceCodeLabel(dto);
        return "导出任务已提交，任务编号: " + exportId;
    }
}

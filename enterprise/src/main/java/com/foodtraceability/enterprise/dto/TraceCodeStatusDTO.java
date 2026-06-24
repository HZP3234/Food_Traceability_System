package com.foodtraceability.enterprise.dto;

/**
 * 溯源码状态变更请求 DTO
 * <p>
 * 用于启用、禁用、作废等状态变更操作。
 * 操作需二次确认（前端完成），变更原因会被记录。
 *
 * @author GuangYao Liu
 * @since 2026-06-23
 */
public class TraceCodeStatusDTO {

    /** 溯源码值 */
    private String traceCode;

    /**
     * 目标状态：
     * 2 - 激活
     * 3 - 禁用
     * 4 - 作废
     */
    private Integer targetStatus;

    /** 变更原因（禁用/作废时必填） */
    private String reason;

    /** 操作人 */
    private String operator;

    // ==================== Getter / Setter ====================

    public String getTraceCode() { return traceCode; }
    public void setTraceCode(String traceCode) { this.traceCode = traceCode; }

    public Integer getTargetStatus() { return targetStatus; }
    public void setTargetStatus(Integer targetStatus) { this.targetStatus = targetStatus; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
}

package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 溯源码实体 — 对应 t_trace_code 表
 * <p>
 * 管理溯源码全生命周期：未绑定 → 已绑定 → 已激活 → 已禁用/已作废/已过期。
 * 每个溯源码全局唯一，支持单品码、批次码、箱码、托码四种类型。
 *
 * @author GuangYao Liu
 * @since 2026-06-23
 */
@TableName("t_trace_code")
public class TraceCode {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long traceCodeId;

    /** 与现有 fts 表一致的业务唯一标识。 */
    private String traceCodeUuid;

    /** 溯源码值，全局唯一，如 TC202606230001 */
    private String traceCode;

    /**
     * 码类型：
     * 1 - 单品码（SINGLE）
     * 2 - 批次码（BATCH）
     * 3 - 箱码（BOX）
     * 4 - 托码（PALLET）
     */
    private Integer codeType;

    /**
     * 包装层级：
     * 1 - 最小销售单元（UNIT）
     * 2 - 外箱（BOX）
     * 3 - 托盘（PALLET）
     */
    private Integer packageLevel;

    /** 关联产品ID */
    private String productId;

    /** 产品名称（冗余字段，便于查询展示） */
    private String productName;

    /** 责任企业ID */
    @TableField("enterprise_uuid")
    private String enterpriseId;

    /** 责任企业名称（冗余字段，便于查询展示） */
    private String enterpriseName;

    /**
     * 溯源码状态：
     * 0 - 未绑定（UNBOUND）：码已生成但未绑定任何业务数据
     * 1 - 已绑定（BOUND）：已绑定业务数据但未激活
     * 2 - 已激活（ACTIVE）：已通过校验并激活，消费者可扫码查询
     * 3 - 已禁用（DISABLED）：因质量问题/召回等原因被禁用
     * 4 - 已作废（VOIDED）：永久作废，不可再次激活
     * 5 - 已过期（EXPIRED）：超过有效期自动过期
     */
    private Integer traceCodeStatus;

    /** 关联的生产批次号 */
    private String batchNo;

    /** SHA-256 内容哈希值，用于防伪校验 */
    private String contentHash;

    /** 区块链存证记录编号（模拟存证ID） */
    private String proofId;

    /** 区块链交易哈希值 */
    private String txHash;

    /** 批量生成时的本次生成数量 */
    private Integer generateCount;

    /** 批量生成批次号，同一批生成的码共享此编号 */
    @TableField(exist = false)
    private String generateBatchNo;

    /** 禁用原因（仅在状态变更为"已禁用"时填写） */
    @TableField(exist = false)
    private String disableReason;

    /** 作废原因（仅在状态变更为"已作废"时填写） */
    @TableField(exist = false)
    private String voidReason;

    /** 有效期截止时间，过期后消费者扫码提示码已过期 */
    @TableField(exist = false)
    private String expireTime;

    /** 质检结果：1-合格 2-不合格 3-待检 */
    @TableField(exist = false)
    private Integer qualityResult;

    /** 质检报告URL */
    @TableField(exist = false)
    private String qualityReportUrl;

    /** 是否上链：0-否 1-是 */
    @TableField(exist = false)
    private Integer isOnChain;

    /** 生成时间 */
    @TableField(exist = false)
    private String generateTime;

    /** 启用时间（激活时间） */
    @TableField(exist = false)
    private String enableTime;

    /** 操作人 */
    @TableField(exist = false)
    private String operator;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    private String createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    private String updateTime;

    /** 逻辑删除标记：0-正常 1-已删除 */
    private Integer isDeleted;

    // ==================== Getter / Setter ====================

    public Long getTraceCodeId() { return traceCodeId; }
    public void setTraceCodeId(Long traceCodeId) { this.traceCodeId = traceCodeId; }

    public String getTraceCodeUuid() { return traceCodeUuid; }
    public void setTraceCodeUuid(String traceCodeUuid) { this.traceCodeUuid = traceCodeUuid; }

    public String getTraceCode() { return traceCode; }
    public void setTraceCode(String traceCode) { this.traceCode = traceCode; }

    public Integer getCodeType() { return codeType; }
    public void setCodeType(Integer codeType) { this.codeType = codeType; }

    public Integer getPackageLevel() { return packageLevel; }
    public void setPackageLevel(Integer packageLevel) { this.packageLevel = packageLevel; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(String enterpriseId) { this.enterpriseId = enterpriseId; }

    public String getEnterpriseName() { return enterpriseName; }
    public void setEnterpriseName(String enterpriseName) { this.enterpriseName = enterpriseName; }

    public Integer getTraceCodeStatus() { return traceCodeStatus; }
    public void setTraceCodeStatus(Integer traceCodeStatus) { this.traceCodeStatus = traceCodeStatus; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public String getProofId() { return proofId; }
    public void setProofId(String proofId) { this.proofId = proofId; }

    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }

    public Integer getGenerateCount() { return generateCount; }
    public void setGenerateCount(Integer generateCount) { this.generateCount = generateCount; }

    public String getGenerateBatchNo() { return generateBatchNo; }
    public void setGenerateBatchNo(String generateBatchNo) { this.generateBatchNo = generateBatchNo; }

    public String getDisableReason() { return disableReason; }
    public void setDisableReason(String disableReason) { this.disableReason = disableReason; }

    public String getVoidReason() { return voidReason; }
    public void setVoidReason(String voidReason) { this.voidReason = voidReason; }

    public String getExpireTime() { return expireTime; }
    public void setExpireTime(String expireTime) { this.expireTime = expireTime; }

    public Integer getQualityResult() { return qualityResult; }
    public void setQualityResult(Integer qualityResult) { this.qualityResult = qualityResult; }

    public String getQualityReportUrl() { return qualityReportUrl; }
    public void setQualityReportUrl(String qualityReportUrl) { this.qualityReportUrl = qualityReportUrl; }

    public Integer getIsOnChain() {
        if (isOnChain != null) return isOnChain;
        return proofId != null && !proofId.isBlank() && txHash != null && !txHash.isBlank() ? 1 : 0;
    }
    public void setIsOnChain(Integer isOnChain) { this.isOnChain = isOnChain; }

    public String getGenerateTime() { return generateTime != null ? generateTime : createTime; }
    public void setGenerateTime(String generateTime) { this.generateTime = generateTime; }

    public String getEnableTime() {
        return enableTime != null ? enableTime : (Integer.valueOf(2).equals(traceCodeStatus) ? updateTime : null);
    }
    public void setEnableTime(String enableTime) { this.enableTime = enableTime; }

    public String getOperator() { return operator != null ? operator : (updateBy != null ? updateBy : createBy); }
    public void setOperator(String operator) { this.operator = operator; }

    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }

    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}

package com.foodtraceability.enterprise.dto;

import java.util.List;

/**
 * 溯源码详情视图对象 VO
 * <p>
 * 包含溯源码完整信息及所有绑定的业务数据列表，
 * 用于监管端和管理端的详情查看。
 *
 * @author GuangYao Liu
 * @since 2026-06-23
 */
public class TraceCodeDetailVO {

    // ========== 溯源码基本信息 ==========

    /** 溯源码值 */
    private String traceCode;

    /** 码类型名称 */
    private String codeTypeName;

    /** 包装层级名称 */
    private String packageLevelName;

    /** 产品名称 */
    private String productName;

    /** 企业名称 */
    private String enterpriseName;

    /** 批次号 */
    private String batchNo;

    /** 状态名称 */
    private String traceCodeStatusName;

    /** SHA-256 内容哈希 */
    private String contentHash;

    /** 区块链交易哈希 */
    private String txHash;

    /** 区块链存证编号 */
    private String proofId;

    /** 质检结果：1-合格 2-不合格 3-待检 */
    private Integer qualityResult;

    /** 质检报告URL */
    private String qualityReportUrl;

    /** 禁用/作废原因 */
    private String reason;

    /** 生成时间 */
    private String generateTime;

    /** 启用时间 */
    private String enableTime;

    /** 有效期截止时间 */
    private String expireTime;

    /** 操作人 */
    private String operator;

    // ========== 绑定的业务数据 ==========

    /** 绑定的业务记录列表 */
    private List<BindInfo> bindList;

    /**
     * 绑定业务记录简要信息
     */
    public static class BindInfo {
        /** 业务类型：RAW_BATCH/PROD_BATCH/PROCESS_BATCH/INSPECTION/LOGISTICS/SALES_TERMINAL */
        private String bizType;
        /** 业务类型中文名 */
        private String bizTypeName;
        /** 业务数据编码（如批次号、物流单号） */
        private String bizNo;
        /** 绑定时间 */
        private String bindTime;

        public String getBizType() { return bizType; }
        public void setBizType(String bizType) { this.bizType = bizType; }

        public String getBizTypeName() { return bizTypeName; }
        public void setBizTypeName(String bizTypeName) { this.bizTypeName = bizTypeName; }

        public String getBizNo() { return bizNo; }
        public void setBizNo(String bizNo) { this.bizNo = bizNo; }

        public String getBindTime() { return bindTime; }
        public void setBindTime(String bindTime) { this.bindTime = bindTime; }
    }

    // ==================== Getter / Setter ====================

    public String getTraceCode() { return traceCode; }
    public void setTraceCode(String traceCode) { this.traceCode = traceCode; }

    public String getCodeTypeName() { return codeTypeName; }
    public void setCodeTypeName(String codeTypeName) { this.codeTypeName = codeTypeName; }

    public String getPackageLevelName() { return packageLevelName; }
    public void setPackageLevelName(String packageLevelName) { this.packageLevelName = packageLevelName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getEnterpriseName() { return enterpriseName; }
    public void setEnterpriseName(String enterpriseName) { this.enterpriseName = enterpriseName; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public String getTraceCodeStatusName() { return traceCodeStatusName; }
    public void setTraceCodeStatusName(String traceCodeStatusName) { this.traceCodeStatusName = traceCodeStatusName; }

    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }

    public String getProofId() { return proofId; }
    public void setProofId(String proofId) { this.proofId = proofId; }

    public Integer getQualityResult() { return qualityResult; }
    public void setQualityResult(Integer qualityResult) { this.qualityResult = qualityResult; }

    public String getQualityReportUrl() { return qualityReportUrl; }
    public void setQualityReportUrl(String qualityReportUrl) { this.qualityReportUrl = qualityReportUrl; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getGenerateTime() { return generateTime; }
    public void setGenerateTime(String generateTime) { this.generateTime = generateTime; }

    public String getEnableTime() { return enableTime; }
    public void setEnableTime(String enableTime) { this.enableTime = enableTime; }

    public String getExpireTime() { return expireTime; }
    public void setExpireTime(String expireTime) { this.expireTime = expireTime; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public List<BindInfo> getBindList() { return bindList; }
    public void setBindList(List<BindInfo> bindList) { this.bindList = bindList; }
}

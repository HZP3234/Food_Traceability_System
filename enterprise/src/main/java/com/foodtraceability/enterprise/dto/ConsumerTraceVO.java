package com.foodtraceability.enterprise.dto;

import java.util.List;

/**
 * 消费者扫码查询结果视图对象 VO
 * <p>
 * 面向消费者的公开接口返回对象，展示产品溯源信息、
 * 流转轨迹、质检结果、链上校验结果及风险提示。
 *
 * @author GuangYao Liu
 * @since 2026-06-23
 */
public class ConsumerTraceVO {

    // ========== 产品基础信息 ==========

    /** 产品名称 */
    private String productName;

    /** 产品批次号 */
    private String batchNo;

    /** 生产企业名称 */
    private String enterpriseName;

    /** 溯源码状态 */
    private String codeStatus;

    /** 质检结果：合格/不合格/待检 */
    private String qualityResult;

    // ========== 流转轨迹 ==========

    /** 流转节点列表（原料→加工→生产→物流→销售） */
    private List<TraceNode> traceNodes;

    /**
     * 流转轨迹节点
     */
    public static class TraceNode {
        /** 节点类型：原料/加工/生产/物流/销售 */
        private String nodeType;
        /** 节点名称 */
        private String nodeName;
        /** 节点编码 */
        private String nodeCode;
        /** 记录时间 */
        private String recordTime;
        /** 详细信息 */
        private String detail;
        /** 温度 */
        private String temperature;
        /** 湿度 */
        private String humidity;
        /** 是否异常：0-正常 1-异常 */
        private Integer isException;

        public String getNodeType() { return nodeType; }
        public void setNodeType(String nodeType) { this.nodeType = nodeType; }

        public String getNodeName() { return nodeName; }
        public void setNodeName(String nodeName) { this.nodeName = nodeName; }

        public String getNodeCode() { return nodeCode; }
        public void setNodeCode(String nodeCode) { this.nodeCode = nodeCode; }

        public String getRecordTime() { return recordTime; }
        public void setRecordTime(String recordTime) { this.recordTime = recordTime; }

        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }

        public String getTemperature() { return temperature; }
        public void setTemperature(String temperature) { this.temperature = temperature; }

        public String getHumidity() { return humidity; }
        public void setHumidity(String humidity) { this.humidity = humidity; }

        public Integer getIsException() { return isException; }
        public void setIsException(Integer isException) { this.isException = isException; }
    }

    // ========== 链上校验 ==========

    /** 链上校验结果 */
    private String chainVerifyResult;

    /** 区块链交易哈希 */
    private String txHash;

    /** 校验说明 */
    private String verifyMessage;

    // ========== 风险提示 ==========

    /** 风险等级：低/中/高/重大 */
    private String riskLevel;

    /** 风险建议 */
    private String riskSuggestion;

    // ========== 扫码日志信息 ==========

    /** 扫码时间 */
    private String scanTime;

    /** 查询结果状态 */
    private String queryResult;

    // ==================== Getter / Setter ====================

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public String getEnterpriseName() { return enterpriseName; }
    public void setEnterpriseName(String enterpriseName) { this.enterpriseName = enterpriseName; }

    public String getCodeStatus() { return codeStatus; }
    public void setCodeStatus(String codeStatus) { this.codeStatus = codeStatus; }

    public String getQualityResult() { return qualityResult; }
    public void setQualityResult(String qualityResult) { this.qualityResult = qualityResult; }

    public List<TraceNode> getTraceNodes() { return traceNodes; }
    public void setTraceNodes(List<TraceNode> traceNodes) { this.traceNodes = traceNodes; }

    public String getChainVerifyResult() { return chainVerifyResult; }
    public void setChainVerifyResult(String chainVerifyResult) { this.chainVerifyResult = chainVerifyResult; }

    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }

    public String getVerifyMessage() { return verifyMessage; }
    public void setVerifyMessage(String verifyMessage) { this.verifyMessage = verifyMessage; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getRiskSuggestion() { return riskSuggestion; }
    public void setRiskSuggestion(String riskSuggestion) { this.riskSuggestion = riskSuggestion; }

    public String getScanTime() { return scanTime; }
    public void setScanTime(String scanTime) { this.scanTime = scanTime; }

    public String getQueryResult() { return queryResult; }
    public void setQueryResult(String queryResult) { this.queryResult = queryResult; }
}

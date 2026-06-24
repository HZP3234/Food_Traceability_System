package com.foodtraceability.enterprise.dto;

/**
 * 溯源码生成请求 DTO
 * <p>
 * 用于单条溯源码生成接口，包含产品信息、码类型、包装层级及
 * 需要绑定的业务数据列表。
 *
 * @author GuangYao Liu
 * @since 2026-06-23
 */
public class TraceCodeGenerateDTO {

    /** 码类型：1-单品码 2-批次码 3-箱码 4-托码 */
    private Integer codeType;

    /** 包装层级：1-最小销售单元 2-外箱 3-托盘 */
    private Integer packageLevel;

    /** 产品ID */
    private String productId;

    /** 产品名称 */
    private String productName;

    /** 责任企业ID */
    private String enterpriseId;

    /** 责任企业名称 */
    private String enterpriseName;

    /** 关联生产批次号 */
    private String batchNo;

    /** 质检结果：1-合格 2-不合格 3-待检 */
    private Integer qualityResult;

    /** 质检报告URL */
    private String qualityReportUrl;

    /** 有效期截止时间（yyyy-MM-dd HH:mm:ss） */
    private String expireTime;

    /** 操作人 */
    private String operator;

    // ==================== Getter / Setter ====================

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

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public Integer getQualityResult() { return qualityResult; }
    public void setQualityResult(Integer qualityResult) { this.qualityResult = qualityResult; }

    public String getQualityReportUrl() { return qualityReportUrl; }
    public void setQualityReportUrl(String qualityReportUrl) { this.qualityReportUrl = qualityReportUrl; }

    public String getExpireTime() { return expireTime; }
    public void setExpireTime(String expireTime) { this.expireTime = expireTime; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
}

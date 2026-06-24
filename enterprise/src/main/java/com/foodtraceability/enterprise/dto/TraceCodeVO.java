package com.foodtraceability.enterprise.dto;

/**
 * 溯源码列表视图对象 VO
 * <p>
 * 用于分页查询列表、批量生成结果等场景的数据展示。
 *
 * @author GuangYao Liu
 * @since 2026-06-23
 */
public class TraceCodeVO {

    /** 主键ID */
    private Integer traceCodeId;

    /** 溯源码值 */
    private String traceCode;

    /** 码类型：1-单品码 2-批次码 3-箱码 4-托码 */
    private Integer codeType;

    /** 码类型名称（中文） */
    private String codeTypeName;

    /** 包装层级：1-最小销售单元 2-外箱 3-托盘 */
    private Integer packageLevel;

    /** 包装层级名称（中文） */
    private String packageLevelName;

    /** 产品名称 */
    private String productName;

    /** 企业名称 */
    private String enterpriseName;

    /** 批次号 */
    private String batchNo;

    /** 溯源码状态：0-未绑定 1-已绑定 2-已激活 3-已禁用 4-已作废 5-已过期 */
    private Integer traceCodeStatus;

    /** 状态名称（中文） */
    private String traceCodeStatusName;

    /** SHA-256 内容哈希 */
    private String contentHash;

    /** 是否上链：0-否 1-是 */
    private Integer isOnChain;

    /** 生成时间 */
    private String generateTime;

    /** 启用时间 */
    private String enableTime;

    /** 有效期截止时间 */
    private String expireTime;

    /** 操作人 */
    private String operator;

    /** 创建时间 */
    private String createTime;

    // ==================== Getter / Setter ====================

    public Integer getTraceCodeId() { return traceCodeId; }
    public void setTraceCodeId(Integer traceCodeId) { this.traceCodeId = traceCodeId; }

    public String getTraceCode() { return traceCode; }
    public void setTraceCode(String traceCode) { this.traceCode = traceCode; }

    public Integer getCodeType() { return codeType; }
    public void setCodeType(Integer codeType) { this.codeType = codeType; }

    public String getCodeTypeName() { return codeTypeName; }
    public void setCodeTypeName(String codeTypeName) { this.codeTypeName = codeTypeName; }

    public Integer getPackageLevel() { return packageLevel; }
    public void setPackageLevel(Integer packageLevel) { this.packageLevel = packageLevel; }

    public String getPackageLevelName() { return packageLevelName; }
    public void setPackageLevelName(String packageLevelName) { this.packageLevelName = packageLevelName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getEnterpriseName() { return enterpriseName; }
    public void setEnterpriseName(String enterpriseName) { this.enterpriseName = enterpriseName; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public Integer getTraceCodeStatus() { return traceCodeStatus; }
    public void setTraceCodeStatus(Integer traceCodeStatus) { this.traceCodeStatus = traceCodeStatus; }

    public String getTraceCodeStatusName() { return traceCodeStatusName; }
    public void setTraceCodeStatusName(String traceCodeStatusName) { this.traceCodeStatusName = traceCodeStatusName; }

    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public Integer getIsOnChain() { return isOnChain; }
    public void setIsOnChain(Integer isOnChain) { this.isOnChain = isOnChain; }

    public String getGenerateTime() { return generateTime; }
    public void setGenerateTime(String generateTime) { this.generateTime = generateTime; }

    public String getEnableTime() { return enableTime; }
    public void setEnableTime(String enableTime) { this.enableTime = enableTime; }

    public String getExpireTime() { return expireTime; }
    public void setExpireTime(String expireTime) { this.expireTime = expireTime; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}

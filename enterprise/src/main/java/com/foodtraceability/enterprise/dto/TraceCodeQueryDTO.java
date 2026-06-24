package com.foodtraceability.enterprise.dto;

/**
 * 溯源码分页查询请求 DTO
 * <p>
 * 支持按码类型、状态、产品名称、企业名称、批次号、创建时间范围等
 * 多条件组合分页查询。
 *
 * @author GuangYao Liu
 * @since 2026-06-23
 */
public class TraceCodeQueryDTO {

    /** 溯源码值（模糊匹配） */
    private String traceCode;

    /** 码类型：1-单品码 2-批次码 3-箱码 4-托码 */
    private Integer codeType;

    /** 溯源码状态：0-未绑定 1-已绑定 2-已激活 3-已禁用 4-已作废 5-已过期 */
    private Integer traceCodeStatus;

    /** 产品名称（模糊匹配） */
    private String productName;

    /** 企业名称（模糊匹配） */
    private String enterpriseName;

    /** 批次号（模糊匹配） */
    private String batchNo;

    /** 是否上链：0-否 1-是 */
    private Integer isOnChain;

    /** 创建时间起始（yyyy-MM-dd HH:mm:ss） */
    private String createTimeStart;

    /** 创建时间截止（yyyy-MM-dd HH:mm:ss） */
    private String createTimeEnd;

    /** 页码（从 1 开始） */
    private Integer page = 1;

    /** 每页条数 */
    private Integer pageSize = 20;

    // ==================== Getter / Setter ====================

    public String getTraceCode() { return traceCode; }
    public void setTraceCode(String traceCode) { this.traceCode = traceCode; }

    public Integer getCodeType() { return codeType; }
    public void setCodeType(Integer codeType) { this.codeType = codeType; }

    public Integer getTraceCodeStatus() { return traceCodeStatus; }
    public void setTraceCodeStatus(Integer traceCodeStatus) { this.traceCodeStatus = traceCodeStatus; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getEnterpriseName() { return enterpriseName; }
    public void setEnterpriseName(String enterpriseName) { this.enterpriseName = enterpriseName; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public Integer getIsOnChain() { return isOnChain; }
    public void setIsOnChain(Integer isOnChain) { this.isOnChain = isOnChain; }

    public String getCreateTimeStart() { return createTimeStart; }
    public void setCreateTimeStart(String createTimeStart) { this.createTimeStart = createTimeStart; }

    public String getCreateTimeEnd() { return createTimeEnd; }
    public void setCreateTimeEnd(String createTimeEnd) { this.createTimeEnd = createTimeEnd; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}

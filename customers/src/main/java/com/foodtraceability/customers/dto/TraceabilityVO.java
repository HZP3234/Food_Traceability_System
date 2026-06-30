package com.foodtraceability.customers.dto;

import com.foodtraceability.customers.entity.TraceabilityNode;
import lombok.Data;

import java.util.List;

@Data
public class TraceabilityVO {

    /** 溯源码 */
    private String traceCode;

    /** 批次号 */
    private String productBatchNo;

    /** 商品名称 */
    private String productName;

    /** 商品规格 */
    private String productSpec;

    /** 生产企业 */
    private String manufacturer;

    /** 产地 */
    private String origin;

    /** 供应商名称 */
    private String supplierName;

    /** 原料批次号 */
    private String rawBatchNo;

    /** 生产日期 */
    private String productionDate;

    /** 保质期至 */
    private String expirationDate;

    /** 生产线 */
    private String productionLine;

    // ========== 企业信息（来自 t_enterprise 表） ==========

    /** 企业类型: 1-供应商 2-加工商 3-物流商 4-零售商 */
    private Integer enterpriseType;

    /** 统一社会信用代码 */
    private String certNo;

    /** 注册地址 */
    private String address;

    /** 联系电话 */
    private String contactPhone;

    /** 联系人 */
    private String contactPerson;

    // ========== 区块链 ==========

    /** 区块链交易哈希 */
    private String txHash;

    /** 溯源节点 */
    private List<TraceabilityNode> nodes;
}

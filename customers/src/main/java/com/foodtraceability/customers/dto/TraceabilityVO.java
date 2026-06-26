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

    /** 生产日期 */
    private String productionDate;

    /** 保质期至 */
    private String expirationDate;

    /** 生产线 */
    private String productionLine;

    /** 质检结果: 1-合格 2-不合格 */
    private Integer checkResult;

    /** 区块链交易哈希 */
    private String txHash;

    /** 溯源节点 */
    private List<TraceabilityNode> nodes;
}

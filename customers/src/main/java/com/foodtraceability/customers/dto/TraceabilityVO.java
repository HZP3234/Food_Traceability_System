package com.foodtraceability.customers.dto;

import com.foodtraceability.customers.entity.TraceabilityNode;
import lombok.Data;

import java.util.List;

@Data
public class TraceabilityVO {

    /** 批次号 */
    private String productBatchNo;

    /** 商品名称 */
    private String productName;

    /** 生产企业 */
    private String manufacturer;

    /** 生产日期 */
    private String productionDate;

    /** 生产线 */
    private String productionLine;

    /** 质检结果: 1-合格 2-不合格 */
    private Integer checkResult;

    /** 区块链交易哈希 */
    private String txHash;

    /** 溯源节点 */
    private List<TraceabilityNode> nodes;
}

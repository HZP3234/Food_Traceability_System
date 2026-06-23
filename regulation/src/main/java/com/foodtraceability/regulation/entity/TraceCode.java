package com.foodtraceability.regulation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 溯源码 — 对应 t_trace_code 表 (需求 3.2.10)
 */
@Data
@TableName("t_trace_code")
public class TraceCode {

    @TableId(value = "trace_code_id", type = IdType.AUTO)
    private Long traceCodeId;

    /** 溯源码UUID */
    private String traceCodeUuid;

    /** 溯源码 (全局唯一) */
    private String traceCode;

    /** 编码类型: 1-单品码 2-箱码 3-托盘码 */
    private Integer codeType;

    /** 包装层级 */
    private Integer packageLevel;

    /** 产品ID */
    private String productId;

    /** 产品名称 */
    private String productName;

    /** 企业UUID */
    private String enterpriseUuid;

    /** 企业名称 */
    private String enterpriseName;

    /** 溯源码状态: 0-正常 1-禁用 2-作废 */
    private Integer traceCodeStatus;

    /** 内容Hash (SHA-256) */
    private String contentHash;

    /** 存证ID (区块链/模拟存证) */
    private String proofId;

    /** 交易Hash (区块链) */
    private String txHash;

    /** 生成次数 */
    private Integer generateCount;

    /** 批次号 */
    private String batchNo;

    /** 禁用原因 */
    private String disableReason;

    /** 作废原因 */
    private String voidReason;

    /** 过期时间 */
    private LocalDateTime expireTime;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String createBy;
    private String updateBy;

    @TableLogic
    private Integer isDeleted;
}

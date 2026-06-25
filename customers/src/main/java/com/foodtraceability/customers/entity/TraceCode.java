package com.foodtraceability.customers.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_trace_code")
public class TraceCode implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long traceCodeId;

    private String traceCodeUuid;

    private String traceCode;

    private Integer codeType;

    private Integer packageLevel;

    private String productId;

    private String productName;

    private String enterpriseUuid;

    private String enterpriseName;

    private Integer traceCodeStatus;

    private String batchNo;

    private String contentHash;

    private String proofId;

    private String txHash;

    private Integer generateCount;

    private String disableReason;

    private String voidReason;

    private LocalDateTime expireTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

    @TableLogic
    private Integer isDeleted;
}

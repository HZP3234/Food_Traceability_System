package com.foodtraceability.customers.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("traceability_node")
public class TraceabilityNode implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String productBatchNo;

    private String nodeName;

    private String nodeDescription;

    private LocalDateTime nodeTime;

    private String location;

    private String operator;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

package com.foodtraceability.customers.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("product_traceability")
public class ProductTraceability implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String productBatchNo;

    private String productName;

    private String productSpec;

    private String manufacturer;

    private String origin;

    private LocalDate productionDate;

    private LocalDate expirationDate;

    private String qrCodeUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

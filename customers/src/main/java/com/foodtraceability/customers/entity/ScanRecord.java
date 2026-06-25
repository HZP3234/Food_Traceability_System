package com.foodtraceability.customers.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("scan_record")
public class ScanRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String productBatchNo;

    private String scanIp;

    private String scanLocation;

    private String userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime scanTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

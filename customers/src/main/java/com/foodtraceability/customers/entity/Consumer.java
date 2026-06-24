package com.foodtraceability.customers.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_consumer_info")
public class Consumer {

    @TableId(type = IdType.AUTO)
    private Long consumerId;

    private String consumerUuid;

    private String nickName;

    private String realName;

    private String phone;

    private String gender;

    private String region;

    private LocalDateTime lastScanTime;

    private Integer totalScans;

    private Integer complaintCount;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

    @TableLogic
    private Integer isDeleted;
}

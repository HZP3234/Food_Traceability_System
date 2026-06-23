package com.foodtraceability.customers.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("complaint")
public class Complaint implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String complaintNo;

    private String productBatchNo;

    private String productName;

    private String consumerName;

    private String consumerPhone;

    private Integer complaintType;

    private String complaintTitle;

    private String complaintContent;

    private String imageUrls;

    private Integer status;

    private String feedbackContent;

    private LocalDateTime feedbackTime;

    private String feedbackBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

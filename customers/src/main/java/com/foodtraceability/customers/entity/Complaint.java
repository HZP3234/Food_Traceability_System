package com.foodtraceability.customers.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_complaint_record")
public class Complaint implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String complaintNo;

    @TableField("product_batch_no")
    private String productBatchNo;

    @TableField("product_name")
    private String productName;

    @TableField("consumer_name")
    private String consumerName;

    @TableField("consumer_phone")
    private String consumerPhone;

    @TableField("consumer_id")
    private Long consumerId;

    private Integer complaintType;

    @TableField("complaint_title")
    private String complaintTitle;

    @TableField("complaint_content")
    private String complaintContent;

    @TableField("image_urls")
    private String imageUrls;

    private Integer status;

    @TableField("feedback_content")
    private String feedbackContent;

    @TableField("feedback_time")
    private LocalDateTime feedbackTime;

    @TableField("feedback_by")
    private String feedbackBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}

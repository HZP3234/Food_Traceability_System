package com.foodtraceability.customers.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 企业信息 — 对应 t_enterprise 表（消费者端只读）
 */
@Data
@TableName("t_enterprise")
public class Enterprise {

    @TableId(value = "enterprise_id", type = IdType.AUTO)
    private Long enterpriseId;

    private String enterpriseUuid;
    private String enterpriseName;
    private Integer enterpriseType;
    private String certNo;
    private String address;
    private String contactPhone;
    private String contactPerson;
    private Integer riskLevel;
    private Integer status;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private java.time.LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private java.time.LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}

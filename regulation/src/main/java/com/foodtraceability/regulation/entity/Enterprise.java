package com.foodtraceability.regulation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业资质信息 — 对应 t_enterprise 表 (需求 3.2.9)
 */
@Data
@TableName("t_enterprise")
public class Enterprise {

    @TableId(value = "enterprise_id", type = IdType.AUTO)
    private Long enterpriseId;

    /** 企业UUID (全局唯一标识) */
    private String enterpriseUuid;

    /** 企业名称 */
    private String enterpriseName;

    /** 企业类型: 1-供应商 2-加工商 3-物流商 4-零售商 */
    private Integer enterpriseType;

    /** 统一社会信用代码 */
    private String certNo;

    /** 注册地址 */
    private String address;

    /** 联系电话 */
    private String contactPhone;

    /** 联系人 */
    private String contactPerson;

    /** 风险等级: 1-低 2-中 3-高 */
    private Integer riskLevel;

    /** 状态: 1-正常 0-停用 */
    private Integer status;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}

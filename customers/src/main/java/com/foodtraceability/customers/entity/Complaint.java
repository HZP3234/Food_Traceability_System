package com.foodtraceability.customers.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 投诉实体 — 对应 t_complaint_record 表（与 regulation 模块共享）
 */
@Data
@TableName("t_complaint_record")
public class Complaint implements Serializable {

    @TableId(value = "complaint_record_id", type = IdType.AUTO)
    private Long complaintRecordId;

    /** 投诉编号，格式：TS+yyyyMMdd+6位流水号 */
    private String complaintNo;

    /** 关联溯源码 */
    private String traceCode;

    /** 关联批次号 */
    private String batchNumber;

    /** 被投诉企业ID */
    private String enterpriseUuid;

    /** 被投诉企业名称 */
    private String enterpriseName;

    /** 投诉人UUID */
    private String consumerUuid;

    /** 投诉人姓名 */
    private String complainantName;

    /** 联系电话 */
    private String phone;

    /** 是否匿名: 0-否 1-是 */
    private Integer isAnonymous;

    /** 投诉类型: 1-产品质量 2-食品安全 3-包装问题 4-虚假宣传 5-其他 */
    private Integer complaintType;

    /** 投诉描述 */
    private String description;

    /** 照片URL列表，逗号分隔 */
    private String photoUrls;

    /** 优先级: 1-普通 2-紧急 3-重大 */
    private Integer priority;

    /** 状态: 1-已提交 2-处理中 3-已处理 4-已驳回 */
    private Integer status;

    /** 处理人ID */
    private String handlerId;

    /** 处理人姓名 */
    private String handlerName;

    /** 处理结论 */
    private String handlingConclusion;

    /** 处理凭证URL列表 */
    private String handlingProofUrls;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 受理时间 */
    private LocalDateTime acceptTime;

    /** 办结时间 */
    private LocalDateTime closeTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private String createBy;
    private String updateBy;

    @TableLogic
    private Integer isDeleted;
}

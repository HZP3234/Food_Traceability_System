package com.foodtraceability.regulation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 投诉处理日志实体 — 对应 t_complaint_handle_log 表
 * <p>
 * 记录投诉从提交到关闭的每一次状态变更操作，形成完整处理链路。
 * </p>
 */
@Data
@TableName("t_complaint_handle_log")
public class ComplaintHandleLog implements Serializable {

    @TableId(value = "complaint_handle_log_id", type = IdType.AUTO)
    private Long complaintHandleLogId;

    /** 投诉编号 */
    private String complaintNo;

    /**
     * 操作动作:
     * SUBMIT   — 消费者提交
     * ACCEPT   — 监管受理
     * INVESTIGATE — 调查取证
     * RESPOND  — 回复反馈
     * CLOSE    — 办结关闭
     * REJECT   — 驳回
     */
    private String action;

    /** 操作前状态 */
    private Integer beforeStatus;

    /** 操作后状态 */
    private Integer afterStatus;

    /** 操作备注/说明 */
    private String remark;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 操作人角色: 1-企业 2-监管部门 3-AI客服 */
    private Integer operatorRole;

    private String createBy;
    private String updateBy;

    @TableLogic
    private Integer isDeleted;
}

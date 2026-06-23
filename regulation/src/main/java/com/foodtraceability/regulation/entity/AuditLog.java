package com.foodtraceability.regulation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作审计日志 — 对应 t_audit_log 表 (需求 3.2.11)
 */
@Data
@TableName("t_audit_log")
public class AuditLog {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /** 日志UUID */
    private String logUuid;

    /** 操作人ID */
    private String operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 操作时间 */
    private LocalDateTime operationTime;

    /** 操作类型: 1-新增 2-修改 3-删除 4-查询 5-导出 */
    private Integer actionType;

    /** 操作目标ID */
    private String targetId;

    /** 操作目标描述 */
    private String targetDesc;

    /** 操作前数据 (JSON/长文本) */
    private String beforeData;

    /** 操作后数据 (JSON/长文本) */
    private String afterData;

    /** 操作结果: 1-成功 0-失败 */
    private Integer operationResult;

    /** 失败原因 */
    private String failureReason;

    /** 日志Hash (SHA-256，含前一条的Hash形成链式结构) */
    private String logHash;

    /** 是否异常: 1-异常 0-正常 */
    private Integer isAbnormal;

    /** 异常描述 */
    private String abnormalDesc;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String createBy;
    private String updateBy;

    @TableLogic
    private Integer isDeleted;
}

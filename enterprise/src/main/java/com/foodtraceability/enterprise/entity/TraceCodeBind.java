package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 溯源码绑定关系实体 — 对应 t_trace_code_bind 表
 * <p>
 * 一个溯源码可以绑定多条不同类型的业务记录：
 * 原料批次、生产批次、加工批次、质检记录、物流单、销售终端等。
 * 每条绑定记录记录溯源码与某一条业务数据的关联关系。
 *
 * @author GuangYao Liu
 * @since 2026-06-23
 */
@TableName("t_trace_code_bind")
public class TraceCodeBind {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Integer bindId;

    /** 溯源码值（外键关联 t_trace_code.trace_code） */
    private String traceCode;

    /**
     * 业务类型：
     * RAW_BATCH       - 原料批次
     * PROD_BATCH      - 生产批次
     * PROCESS_BATCH   - 加工批次
     * INSPECTION      - 质检记录
     * LOGISTICS       - 物流单号
     * SALES_TERMINAL  - 销售终端
     */
    private String bizType;

    /** 业务数据编号（对应各业务表的主键ID） */
    private String bizId;

    /** 业务数据编码（如批次号、物流单号，冗余字段便于展示） */
    private String bizNo;

    /** 绑定时间 */
    private String createTime;

    /** 绑定操作人 */
    private String operator;

    /** 逻辑删除标记：0-正常 1-已删除 */
    private Integer isDeleted;

    // ==================== Getter / Setter ====================

    public Integer getBindId() { return bindId; }
    public void setBindId(Integer bindId) { this.bindId = bindId; }

    public String getTraceCode() { return traceCode; }
    public void setTraceCode(String traceCode) { this.traceCode = traceCode; }

    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }

    public String getBizId() { return bizId; }
    public void setBizId(String bizId) { this.bizId = bizId; }

    public String getBizNo() { return bizNo; }
    public void setBizNo(String bizNo) { this.bizNo = bizNo; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}

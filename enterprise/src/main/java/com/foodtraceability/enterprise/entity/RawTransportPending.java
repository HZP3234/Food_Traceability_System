package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 原料运输待匹配记录 — 对应 t_raw_transport_pending 表
 * 供应商上传运输单号后等待冷链物流商匹配 CcTransport 订单
 */
@TableName("t_raw_transport_pending")
public class RawTransportPending {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String rawDetailId;      // 关联的溯源详情ID
    private String rawBatchNo;        // 关联的原料批次号（匹配后回填）
    private String transportOrderNo;  // 供应商填写的运输单号
    private String supplierName;      // 供应商名称
    private int matchStatus;          // 1=待匹配, 2=已匹配
    private String matchTime;         // 匹配时间
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRawDetailId() { return rawDetailId; }
    public void setRawDetailId(String rawDetailId) { this.rawDetailId = rawDetailId; }

    public String getRawBatchNo() { return rawBatchNo; }
    public void setRawBatchNo(String rawBatchNo) { this.rawBatchNo = rawBatchNo; }

    public String getTransportOrderNo() { return transportOrderNo; }
    public void setTransportOrderNo(String transportOrderNo) { this.transportOrderNo = transportOrderNo; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public int getMatchStatus() { return matchStatus; }
    public void setMatchStatus(int matchStatus) { this.matchStatus = matchStatus; }

    public String getMatchTime() { return matchTime; }
    public void setMatchTime(String matchTime) { this.matchTime = matchTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }

    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }

    public int getIsDeleted() { return isDeleted; }
    public void setIsDeleted(int isDeleted) { this.isDeleted = isDeleted; }
}

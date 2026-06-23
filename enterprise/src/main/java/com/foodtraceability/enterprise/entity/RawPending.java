package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_raw_pending")
public class RawPending {
    @TableId(type = IdType.AUTO)
    private int rawPendingId;
    private String pendingCode;
    private String productName;
    private String productCategory;
    private String supplierName;
    private int amount;
    private String rawDetailId;
    private String matchedBatchNo;
    private int pendingStatus;
    private String uploadTime;
    private String matchTime;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public int getRawPendingId() { return rawPendingId; }
    public void setRawPendingId(int rawPendingId) { this.rawPendingId = rawPendingId; }

    public String getPendingCode() { return pendingCode; }
    public void setPendingCode(String pendingCode) { this.pendingCode = pendingCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getRawDetailId() { return rawDetailId; }
    public void setRawDetailId(String rawDetailId) { this.rawDetailId = rawDetailId; }

    public String getMatchedBatchNo() { return matchedBatchNo; }
    public void setMatchedBatchNo(String matchedBatchNo) { this.matchedBatchNo = matchedBatchNo; }

    public int getPendingStatus() { return pendingStatus; }
    public void setPendingStatus(int pendingStatus) { this.pendingStatus = pendingStatus; }

    public String getUploadTime() { return uploadTime; }
    public void setUploadTime(String uploadTime) { this.uploadTime = uploadTime; }

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

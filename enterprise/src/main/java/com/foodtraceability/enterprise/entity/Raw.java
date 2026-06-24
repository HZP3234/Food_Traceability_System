package com.foodtraceability.enterprise.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_raw_batch")
public class Raw {
    @TableId(type = IdType.AUTO)
    private Long rawBatchId;
    private String batchNo;
    private String productName;
    private String productCategory;
    private BigDecimal amount;
    private String unit;
    private String supplierId;
    private String supplierName;
    private String warehouse;
    private int storageMethod;
    private String shelfLife;
    private String purchaseDate;
    private Integer checkResult;
    private Integer batchStatus;
    private String detailId;
    private Integer detailStatus;
    private String dataHash;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Long getRawBatchId() { return rawBatchId; }
    public void setRawBatchId(Long rawBatchId) { this.rawBatchId = rawBatchId; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }

    public int getStorageMethod() { return storageMethod; }
    public void setStorageMethod(int storageMethod) { this.storageMethod = storageMethod; }

    public String getShelfLife() { return shelfLife; }
    public void setShelfLife(String shelfLife) { this.shelfLife = shelfLife; }

    public String getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }

    public Integer getCheckResult() { return checkResult; }
    public void setCheckResult(Integer checkResult) { this.checkResult = checkResult; }

    public Integer getBatchStatus() { return batchStatus; }
    public void setBatchStatus(Integer batchStatus) { this.batchStatus = batchStatus; }

    public String getDetailId() { return detailId; }
    public void setDetailId(String detailId) { this.detailId = detailId; }

    public Integer getDetailStatus() { return detailStatus; }
    public void setDetailStatus(Integer detailStatus) { this.detailStatus = detailStatus; }

    public String getDataHash() { return dataHash; }
    public void setDataHash(String dataHash) { this.dataHash = dataHash; }

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

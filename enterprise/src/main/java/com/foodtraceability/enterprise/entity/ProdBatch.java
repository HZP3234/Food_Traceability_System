package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_prod_batch")
public class ProdBatch {
    @TableId(type = IdType.AUTO)
    private Integer prodBatchId;
    private String batchNo;
    private String productName;
    private String processBatchNo;
    private String productionLine;
    private int plannedAmount;
    private int actualAmount;
    private String productionDate;
    private int checkResult;
    private int codeStatus;
    private int batchStatus;
    private String dataHash;
    private String chainHash;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Integer getProdBatchId() { return prodBatchId; }
    public void setProdBatchId(Integer prodBatchId) { this.prodBatchId = prodBatchId; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProcessBatchNo() { return processBatchNo; }
    public void setProcessBatchNo(String processBatchNo) { this.processBatchNo = processBatchNo; }

    public String getProductionLine() { return productionLine; }
    public void setProductionLine(String productionLine) { this.productionLine = productionLine; }

    public int getPlannedAmount() { return plannedAmount; }
    public void setPlannedAmount(int plannedAmount) { this.plannedAmount = plannedAmount; }

    public int getActualAmount() { return actualAmount; }
    public void setActualAmount(int actualAmount) { this.actualAmount = actualAmount; }

    public String getProductionDate() { return productionDate; }
    public void setProductionDate(String productionDate) { this.productionDate = productionDate; }

    public int getCheckResult() { return checkResult; }
    public void setCheckResult(int checkResult) { this.checkResult = checkResult; }

    public int getCodeStatus() { return codeStatus; }
    public void setCodeStatus(int codeStatus) { this.codeStatus = codeStatus; }

    public int getBatchStatus() { return batchStatus; }
    public void setBatchStatus(int batchStatus) { this.batchStatus = batchStatus; }

    public String getDataHash() { return dataHash; }
    public void setDataHash(String dataHash) { this.dataHash = dataHash; }

    public String getChainHash() { return chainHash; }
    public void setChainHash(String chainHash) { this.chainHash = chainHash; }

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

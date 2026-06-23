package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_process_batch")
public class ProcessBatch {
    @TableId(type = IdType.AUTO)
    private Integer processBatchId;
    private String batchNo;
    private String productName;
    private String templateName;
    private String rawBatchNo;
    private int plannedAmount;
    private String processDate;
    private String operator;
    private String productionLine;
    private int shift;
    private String actualTemp;
    private String actualDuration;
    private String actualPressure;
    private String actualCoolTemp;
    private String actualFillTemp;
    private String actualPh;
    private String actualViscosity;
    private int batchStatus;
    private String dataHash;
    private String chainHash;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Integer getProcessBatchId() { return processBatchId; }
    public void setProcessBatchId(Integer processBatchId) { this.processBatchId = processBatchId; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public String getRawBatchNo() { return rawBatchNo; }
    public void setRawBatchNo(String rawBatchNo) { this.rawBatchNo = rawBatchNo; }

    public int getPlannedAmount() { return plannedAmount; }
    public void setPlannedAmount(int plannedAmount) { this.plannedAmount = plannedAmount; }

    public String getProcessDate() { return processDate; }
    public void setProcessDate(String processDate) { this.processDate = processDate; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public String getProductionLine() { return productionLine; }
    public void setProductionLine(String productionLine) { this.productionLine = productionLine; }

    public int getShift() { return shift; }
    public void setShift(int shift) { this.shift = shift; }

    public String getActualTemp() { return actualTemp; }
    public void setActualTemp(String actualTemp) { this.actualTemp = actualTemp; }

    public String getActualDuration() { return actualDuration; }
    public void setActualDuration(String actualDuration) { this.actualDuration = actualDuration; }

    public String getActualPressure() { return actualPressure; }
    public void setActualPressure(String actualPressure) { this.actualPressure = actualPressure; }

    public String getActualCoolTemp() { return actualCoolTemp; }
    public void setActualCoolTemp(String actualCoolTemp) { this.actualCoolTemp = actualCoolTemp; }

    public String getActualFillTemp() { return actualFillTemp; }
    public void setActualFillTemp(String actualFillTemp) { this.actualFillTemp = actualFillTemp; }

    public String getActualPh() { return actualPh; }
    public void setActualPh(String actualPh) { this.actualPh = actualPh; }

    public String getActualViscosity() { return actualViscosity; }
    public void setActualViscosity(String actualViscosity) { this.actualViscosity = actualViscosity; }

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

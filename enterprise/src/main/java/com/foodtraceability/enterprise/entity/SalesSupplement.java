package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_sales_supplement")
public class SalesSupplement {
    @TableId(type = IdType.AUTO)
    private Integer supplementId;
    private String supplementCode;
    private String traceBatchId;
    private String traceBatchNo;
    private String terminalCode;
    private String terminalName;
    private String salesCompany;
    private String salesBatchNo;
    private String temperature;
    private String humidity;
    private int storageMethod;
    private int lightCondition;
    private String shelfLife;
    private String locationCode;
    private int quantity;
    private String inboundTime;
    private String operator;
    private int supplementStatus;
    private String dataHash;
    private String chainHash;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Integer getSupplementId() { return supplementId; }
    public void setSupplementId(Integer supplementId) { this.supplementId = supplementId; }

    public String getSupplementCode() { return supplementCode; }
    public void setSupplementCode(String supplementCode) { this.supplementCode = supplementCode; }

    public String getTraceBatchId() { return traceBatchId; }
    public void setTraceBatchId(String traceBatchId) { this.traceBatchId = traceBatchId; }

    public String getTraceBatchNo() { return traceBatchNo; }
    public void setTraceBatchNo(String traceBatchNo) { this.traceBatchNo = traceBatchNo; }

    public String getTerminalCode() { return terminalCode; }
    public void setTerminalCode(String terminalCode) { this.terminalCode = terminalCode; }

    public String getTerminalName() { return terminalName; }
    public void setTerminalName(String terminalName) { this.terminalName = terminalName; }

    public String getSalesCompany() { return salesCompany; }
    public void setSalesCompany(String salesCompany) { this.salesCompany = salesCompany; }

    public String getSalesBatchNo() { return salesBatchNo; }
    public void setSalesBatchNo(String salesBatchNo) { this.salesBatchNo = salesBatchNo; }

    public String getTemperature() { return temperature; }
    public void setTemperature(String temperature) { this.temperature = temperature; }

    public String getHumidity() { return humidity; }
    public void setHumidity(String humidity) { this.humidity = humidity; }

    public int getStorageMethod() { return storageMethod; }
    public void setStorageMethod(int storageMethod) { this.storageMethod = storageMethod; }

    public int getLightCondition() { return lightCondition; }
    public void setLightCondition(int lightCondition) { this.lightCondition = lightCondition; }

    public String getShelfLife() { return shelfLife; }
    public void setShelfLife(String shelfLife) { this.shelfLife = shelfLife; }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getInboundTime() { return inboundTime; }
    public void setInboundTime(String inboundTime) { this.inboundTime = inboundTime; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public int getSupplementStatus() { return supplementStatus; }
    public void setSupplementStatus(int supplementStatus) { this.supplementStatus = supplementStatus; }

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

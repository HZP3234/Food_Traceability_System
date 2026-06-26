package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_sales_order_detail")
public class SalesOrderDetail {
    @TableId(type = IdType.AUTO)
    private Long detailId;
    private String detailCode;
    private Long salesOrderId;
    private String salesOrderCode;
    private String temperature;
    private String humidity;
    private Integer storageMethod;
    private Integer lightCondition;
    private String shelfLife;
    private String locationCode;
    private String inboundTime;
    private Integer actualQuantity;
    private String salesDate;
    private Integer detailStatus;
    private String dataHash;
    private String chainHash;
    private String operator;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private Integer isDeleted;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }

    public String getDetailCode() { return detailCode; }
    public void setDetailCode(String detailCode) { this.detailCode = detailCode; }

    public Long getSalesOrderId() { return salesOrderId; }
    public void setSalesOrderId(Long salesOrderId) { this.salesOrderId = salesOrderId; }

    public String getSalesOrderCode() { return salesOrderCode; }
    public void setSalesOrderCode(String salesOrderCode) { this.salesOrderCode = salesOrderCode; }

    public String getTemperature() { return temperature; }
    public void setTemperature(String temperature) { this.temperature = temperature; }

    public String getHumidity() { return humidity; }
    public void setHumidity(String humidity) { this.humidity = humidity; }

    public Integer getStorageMethod() { return storageMethod; }
    public void setStorageMethod(Integer storageMethod) { this.storageMethod = storageMethod; }

    public Integer getLightCondition() { return lightCondition; }
    public void setLightCondition(Integer lightCondition) { this.lightCondition = lightCondition; }

    public String getShelfLife() { return shelfLife; }
    public void setShelfLife(String shelfLife) { this.shelfLife = shelfLife; }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public String getInboundTime() { return inboundTime; }
    public void setInboundTime(String inboundTime) { this.inboundTime = inboundTime; }

    public Integer getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(Integer actualQuantity) { this.actualQuantity = actualQuantity; }

    public String getSalesDate() { return salesDate; }
    public void setSalesDate(String salesDate) { this.salesDate = salesDate; }

    public Integer getDetailStatus() { return detailStatus; }
    public void setDetailStatus(Integer detailStatus) { this.detailStatus = detailStatus; }

    public String getDataHash() { return dataHash; }
    public void setDataHash(String dataHash) { this.dataHash = dataHash; }

    public String getChainHash() { return chainHash; }
    public void setChainHash(String chainHash) { this.chainHash = chainHash; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

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

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}

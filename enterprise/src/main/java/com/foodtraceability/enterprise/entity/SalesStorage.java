package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_sales_storage")
public class SalesStorage {
    @TableId(type = IdType.AUTO)
    private Integer storageId;
    private String terminalCode;
    private String terminalType;
    private String productType;
    private String temperature;
    private String humidity;
    private int storageMethod;
    private int lightCondition;
    private String shelfLife;
    private String locationCode;
    private String operator;
    private int isAutoFilled;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Integer getStorageId() { return storageId; }
    public void setStorageId(Integer storageId) { this.storageId = storageId; }

    public String getTerminalCode() { return terminalCode; }
    public void setTerminalCode(String terminalCode) { this.terminalCode = terminalCode; }

    public String getTerminalType() { return terminalType; }
    public void setTerminalType(String terminalType) { this.terminalType = terminalType; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

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

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public int getIsAutoFilled() { return isAutoFilled; }
    public void setIsAutoFilled(int isAutoFilled) { this.isAutoFilled = isAutoFilled; }

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

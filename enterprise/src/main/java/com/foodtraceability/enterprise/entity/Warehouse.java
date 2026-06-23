package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_warehouse")
public class Warehouse {
    @TableId(type = IdType.AUTO)
    private Integer warehouseId;
    private String warehouseUuid;
    private String warehouseName;
    private int warehouseType;
    private String address;
    private String capacity;
    private String manager;
    private String tempRange;
    private String humidityRange;
    private int warehouseStatus;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Integer getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseUuid() { return warehouseUuid; }
    public void setWarehouseUuid(String warehouseUuid) { this.warehouseUuid = warehouseUuid; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public int getWarehouseType() { return warehouseType; }
    public void setWarehouseType(int warehouseType) { this.warehouseType = warehouseType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCapacity() { return capacity; }
    public void setCapacity(String capacity) { this.capacity = capacity; }

    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }

    public String getTempRange() { return tempRange; }
    public void setTempRange(String tempRange) { this.tempRange = tempRange; }

    public String getHumidityRange() { return humidityRange; }
    public void setHumidityRange(String humidityRange) { this.humidityRange = humidityRange; }

    public int getWarehouseStatus() { return warehouseStatus; }
    public void setWarehouseStatus(int warehouseStatus) { this.warehouseStatus = warehouseStatus; }

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

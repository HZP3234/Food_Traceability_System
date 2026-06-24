package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_cc_vehicle")
public class CcVehicle { 
    @TableId(type = IdType.AUTO)
    private Integer vehicleId;
    private String plateNo;
    private String vehicleModel;
    private String driverName;
    private String driverPhone;
    private String ownerId;
    private String ownerName;
    private String coldType;
    private String tempRange;
    private Integer vehicleStatus;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }

    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getColdType() { return coldType; }
    public void setColdType(String coldType) { this.coldType = coldType; }

    public String getTempRange() { return tempRange; }
    public void setTempRange(String tempRange) { this.tempRange = tempRange; }

    public Integer getVehicleStatus() { return vehicleStatus; }
    public void setVehicleStatus(Integer vehicleStatus) { this.vehicleStatus = vehicleStatus; }

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

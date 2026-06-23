package com.foodtraceability.enterprise.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_cc_transport")
public class CcTransport {
    @TableId(type = IdType.AUTO)
    private Integer transportId;
    private String orderNo;
    private String plateNo;
    private String driverName;
    private String driverPhone;
    private String productName;
    private String prodBatchNo;
    private String departureId;
    private String departureName;
    private String destinationId;
    private String destinationName;
    private String loadingTemp;
    private String departTime;
    private String estimatedArrival;
    private String actualArrival;
    private int transportMethod;
    private String collectInterval;
    private BigDecimal tempUpper;
    private BigDecimal tempLower;
    private BigDecimal humidUpper;
    private BigDecimal humidLower;
    private String alertMethod;
    private int transportStatus;
    private String dataHash;
    private String chainHash;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Integer getTransportId() { return transportId; }
    public void setTransportId(Integer transportId) { this.transportId = transportId; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProdBatchNo() { return prodBatchNo; }
    public void setProdBatchNo(String prodBatchNo) { this.prodBatchNo = prodBatchNo; }

    public String getDepartureId() { return departureId; }
    public void setDepartureId(String departureId) { this.departureId = departureId; }

    public String getDepartureName() { return departureName; }
    public void setDepartureName(String departureName) { this.departureName = departureName; }

    public String getDestinationId() { return destinationId; }
    public void setDestinationId(String destinationId) { this.destinationId = destinationId; }

    public String getDestinationName() { return destinationName; }
    public void setDestinationName(String destinationName) { this.destinationName = destinationName; }

    public String getLoadingTemp() { return loadingTemp; }
    public void setLoadingTemp(String loadingTemp) { this.loadingTemp = loadingTemp; }

    public String getDepartTime() { return departTime; }
    public void setDepartTime(String departTime) { this.departTime = departTime; }

    public String getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(String estimatedArrival) { this.estimatedArrival = estimatedArrival; }

    public String getActualArrival() { return actualArrival; }
    public void setActualArrival(String actualArrival) { this.actualArrival = actualArrival; }

    public int getTransportMethod() { return transportMethod; }
    public void setTransportMethod(int transportMethod) { this.transportMethod = transportMethod; }

    public String getCollectInterval() { return collectInterval; }
    public void setCollectInterval(String collectInterval) { this.collectInterval = collectInterval; }

    public BigDecimal getTempUpper() { return tempUpper; }
    public void setTempUpper(BigDecimal tempUpper) { this.tempUpper = tempUpper; }

    public BigDecimal getTempLower() { return tempLower; }
    public void setTempLower(BigDecimal tempLower) { this.tempLower = tempLower; }

    public BigDecimal getHumidUpper() { return humidUpper; }
    public void setHumidUpper(BigDecimal humidUpper) { this.humidUpper = humidUpper; }

    public BigDecimal getHumidLower() { return humidLower; }
    public void setHumidLower(BigDecimal humidLower) { this.humidLower = humidLower; }

    public String getAlertMethod() { return alertMethod; }
    public void setAlertMethod(String alertMethod) { this.alertMethod = alertMethod; }

    public int getTransportStatus() { return transportStatus; }
    public void setTransportStatus(int transportStatus) { this.transportStatus = transportStatus; }

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

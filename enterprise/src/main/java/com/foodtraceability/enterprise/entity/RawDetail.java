package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_raw_detail")
public class RawDetail {
    @TableId(type = IdType.AUTO)
    private int rawDetailId;
    private String batchNo;
    private String origin;
    private String farmType;
    private String feedType;
    private int certType;
    private String inspectionNo;
    private String breed;
    private String scaleDesc;
    private String collectDate;
    private String plateNo;
    private String transportTemp;
    private int storageMethod;
    private String shelfLife;
    private String uploader;
    private String uploadTime;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public int getRawDetailId() { return rawDetailId; }
    public void setRawDetailId(int rawDetailId) { this.rawDetailId = rawDetailId; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getFarmType() { return farmType; }
    public void setFarmType(String farmType) { this.farmType = farmType; }

    public String getFeedType() { return feedType; }
    public void setFeedType(String feedType) { this.feedType = feedType; }

    public int getCertType() { return certType; }
    public void setCertType(int certType) { this.certType = certType; }

    public String getInspectionNo() { return inspectionNo; }
    public void setInspectionNo(String inspectionNo) { this.inspectionNo = inspectionNo; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public String getScaleDesc() { return scaleDesc; }
    public void setScaleDesc(String scaleDesc) { this.scaleDesc = scaleDesc; }

    public String getCollectDate() { return collectDate; }
    public void setCollectDate(String collectDate) { this.collectDate = collectDate; }

    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }

    public String getTransportTemp() { return transportTemp; }
    public void setTransportTemp(String transportTemp) { this.transportTemp = transportTemp; }

    public int getStorageMethod() { return storageMethod; }
    public void setStorageMethod(int storageMethod) { this.storageMethod = storageMethod; }

    public String getShelfLife() { return shelfLife; }
    public void setShelfLife(String shelfLife) { this.shelfLife = shelfLife; }

    public String getUploader() { return uploader; }
    public void setUploader(String uploader) { this.uploader = uploader; }

    public String getUploadTime() { return uploadTime; }
    public void setUploadTime(String uploadTime) { this.uploadTime = uploadTime; }

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

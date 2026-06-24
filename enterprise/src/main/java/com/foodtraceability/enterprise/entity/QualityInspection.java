package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
//
@TableName("t_quality_inspection")
public class QualityInspection {
    @TableId(type = IdType.AUTO)
    private Long inspectionId;
    private String inspectionNo;
    private int bizType;
    private String bizBatchNo;
    private int inspectionType;
    private Integer inspectionResult;
    private String inspector;
    private String standard;
    private String inspectionDate;
    private int sensoryCheck;
    private int microbeCheck;
    private String sealCheck;
    private String detailResult;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Long getInspectionId() { return inspectionId; }
    public void setInspectionId(Long inspectionId) { this.inspectionId = inspectionId; }

    public String getInspectionNo() { return inspectionNo; }
    public void setInspectionNo(String inspectionNo) { this.inspectionNo = inspectionNo; }

    public int getBizType() { return bizType; }
    public void setBizType(int bizType) { this.bizType = bizType; }

    public String getBizBatchNo() { return bizBatchNo; }
    public void setBizBatchNo(String bizBatchNo) { this.bizBatchNo = bizBatchNo; }

    public int getInspectionType() { return inspectionType; }
    public void setInspectionType(int inspectionType) { this.inspectionType = inspectionType; }

    public Integer getInspectionResult() { return inspectionResult; }
    public void setInspectionResult(Integer inspectionResult) { this.inspectionResult = inspectionResult; }

    public String getInspector() { return inspector; }
    public void setInspector(String inspector) { this.inspector = inspector; }

    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }

    public String getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(String inspectionDate) { this.inspectionDate = inspectionDate; }

    public int getSensoryCheck() { return sensoryCheck; }
    public void setSensoryCheck(int sensoryCheck) { this.sensoryCheck = sensoryCheck; }

    public int getMicrobeCheck() { return microbeCheck; }
    public void setMicrobeCheck(int microbeCheck) { this.microbeCheck = microbeCheck; }

    public String getSealCheck() { return sealCheck; }
    public void setSealCheck(String sealCheck) { this.sealCheck = sealCheck; }

    public String getDetailResult() { return detailResult; }
    public void setDetailResult(String detailResult) { this.detailResult = detailResult; }

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

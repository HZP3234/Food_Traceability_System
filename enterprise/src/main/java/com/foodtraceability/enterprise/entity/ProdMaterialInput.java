package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
//
@TableName("t_prod_material_input")
public class ProdMaterialInput {
    @TableId(type = IdType.AUTO)
    private Integer inputId;
    private String rawBatchNo;
    private String materialName;
    private int inputAmount;
    private String inputStatus;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Integer getInputId() { return inputId; }
    public void setInputId(Integer inputId) { this.inputId = inputId; }

    public String getRawBatchNo() { return rawBatchNo; }
    public void setRawBatchNo(String rawBatchNo) { this.rawBatchNo = rawBatchNo; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

    public int getInputAmount() { return inputAmount; }
    public void setInputAmount(int inputAmount) { this.inputAmount = inputAmount; }

    public String getInputStatus() { return inputStatus; }
    public void setInputStatus(String inputStatus) { this.inputStatus = inputStatus; }

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

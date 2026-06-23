package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_tech_template")
public class TechTemplate {
    @TableId(type = IdType.AUTO)
    private Integer templateId;
    private String templateName;
    private String version;
    private String applicableProduct;
    private String targetTemp;
    private String duration;
    private String pressure;
    private String coolTemp;
    private String fillTemp;
    private String stirSpeed;
    private String phValue;
    private String viscosity;
    private int cleanLevel;
    private int templateStatus;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Integer getTemplateId() { return templateId; }
    public void setTemplateId(Integer templateId) { this.templateId = templateId; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getApplicableProduct() { return applicableProduct; }
    public void setApplicableProduct(String applicableProduct) { this.applicableProduct = applicableProduct; }

    public String getTargetTemp() { return targetTemp; }
    public void setTargetTemp(String targetTemp) { this.targetTemp = targetTemp; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getPressure() { return pressure; }
    public void setPressure(String pressure) { this.pressure = pressure; }

    public String getCoolTemp() { return coolTemp; }
    public void setCoolTemp(String coolTemp) { this.coolTemp = coolTemp; }

    public String getFillTemp() { return fillTemp; }
    public void setFillTemp(String fillTemp) { this.fillTemp = fillTemp; }

    public String getStirSpeed() { return stirSpeed; }
    public void setStirSpeed(String stirSpeed) { this.stirSpeed = stirSpeed; }

    public String getPhValue() { return phValue; }
    public void setPhValue(String phValue) { this.phValue = phValue; }

    public String getViscosity() { return viscosity; }
    public void setViscosity(String viscosity) { this.viscosity = viscosity; }

    public int getCleanLevel() { return cleanLevel; }
    public void setCleanLevel(int cleanLevel) { this.cleanLevel = cleanLevel; }

    public int getTemplateStatus() { return templateStatus; }
    public void setTemplateStatus(int templateStatus) { this.templateStatus = templateStatus; }

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

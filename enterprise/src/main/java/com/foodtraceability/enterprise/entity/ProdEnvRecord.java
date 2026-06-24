package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
//
@TableName("t_prod_env_record")
public class ProdEnvRecord {
    @TableId(type = IdType.AUTO)
    private Long envRecordId;
    private String productionLine;
    private String collectTime;
    private String collector;
    private String workshopTemp;
    private String workshopHumidity;
    private String cleanLevel;
    private String pressureDiff;
    private String particles;
    private String bacteria;
    private int isAbnormal;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Long getEnvRecordId() { return envRecordId; }
    public void setEnvRecordId(Long envRecordId) { this.envRecordId = envRecordId; }

    public String getProductionLine() { return productionLine; }
    public void setProductionLine(String productionLine) { this.productionLine = productionLine; }

    public String getCollectTime() { return collectTime; }
    public void setCollectTime(String collectTime) { this.collectTime = collectTime; }

    public String getCollector() { return collector; }
    public void setCollector(String collector) { this.collector = collector; }

    public String getWorkshopTemp() { return workshopTemp; }
    public void setWorkshopTemp(String workshopTemp) { this.workshopTemp = workshopTemp; }

    public String getWorkshopHumidity() { return workshopHumidity; }
    public void setWorkshopHumidity(String workshopHumidity) { this.workshopHumidity = workshopHumidity; }

    public String getCleanLevel() { return cleanLevel; }
    public void setCleanLevel(String cleanLevel) { this.cleanLevel = cleanLevel; }

    public String getPressureDiff() { return pressureDiff; }
    public void setPressureDiff(String pressureDiff) { this.pressureDiff = pressureDiff; }

    public String getParticles() { return particles; }
    public void setParticles(String particles) { this.particles = particles; }

    public String getBacteria() { return bacteria; }
    public void setBacteria(String bacteria) { this.bacteria = bacteria; }

    public int getIsAbnormal() { return isAbnormal; }
    public void setIsAbnormal(int isAbnormal) { this.isAbnormal = isAbnormal; }

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

package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 生产批次 — 合并加工批次字段后，直接关联原料批次
 * 链路：原料批次(raw_batch_no) → 生产批次(包含加工参数) → 冷链/销售
 */
@TableName("t_prod_batch")
public class ProdBatch {
    @TableId(type = IdType.AUTO)
    private Long prodBatchId;
    private String batchNo;
    private String productName;

    // ==== 加工相关字段（从 t_process_batch 合并） ====
    private String templateName;      // 工艺模板名称
    private String rawBatchNo;        // 关联原料批次号
    private String processDate;       // 加工日期
    private String operator;          // 操作员
    private int shift;                // 班次 1早班 2中班 3晚班
    private String actualTemp;        // 实际杀菌温度(℃)
    private String actualDuration;    // 实际杀菌时长(s)
    private String actualPressure;    // 实际均质压力(MPa)
    private String actualCoolTemp;    // 实际冷却温度(℃)
    private String actualFillTemp;    // 实际灌装温度(℃)
    private String actualPh;          // 实际pH值
    private String actualViscosity;   // 实际粘度(mPa·s)

    // ==== 生产相关字段 ====
    private String productionLine;
    private int plannedAmount;
    private int actualAmount;
    private String productionDate;
    private Integer checkResult;      // 1合格 2不合格 0未检测
    private Integer codeStatus;       // 1未绑定 2已绑定
    private Integer batchStatus;      // 1待生产 2生产中 3已完成 4已废弃
    private String dataHash;
    private String chainHash;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    // ======== Getter / Setter ========

    public Long getProdBatchId() { return prodBatchId; }
    public void setProdBatchId(Long prodBatchId) { this.prodBatchId = prodBatchId; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public String getRawBatchNo() { return rawBatchNo; }
    public void setRawBatchNo(String rawBatchNo) { this.rawBatchNo = rawBatchNo; }

    public String getProcessDate() { return processDate; }
    public void setProcessDate(String processDate) { this.processDate = processDate; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public int getShift() { return shift; }
    public void setShift(int shift) { this.shift = shift; }

    public String getActualTemp() { return actualTemp; }
    public void setActualTemp(String actualTemp) { this.actualTemp = actualTemp; }

    public String getActualDuration() { return actualDuration; }
    public void setActualDuration(String actualDuration) { this.actualDuration = actualDuration; }

    public String getActualPressure() { return actualPressure; }
    public void setActualPressure(String actualPressure) { this.actualPressure = actualPressure; }

    public String getActualCoolTemp() { return actualCoolTemp; }
    public void setActualCoolTemp(String actualCoolTemp) { this.actualCoolTemp = actualCoolTemp; }

    public String getActualFillTemp() { return actualFillTemp; }
    public void setActualFillTemp(String actualFillTemp) { this.actualFillTemp = actualFillTemp; }

    public String getActualPh() { return actualPh; }
    public void setActualPh(String actualPh) { this.actualPh = actualPh; }

    public String getActualViscosity() { return actualViscosity; }
    public void setActualViscosity(String actualViscosity) { this.actualViscosity = actualViscosity; }

    public String getProductionLine() { return productionLine; }
    public void setProductionLine(String productionLine) { this.productionLine = productionLine; }

    public int getPlannedAmount() { return plannedAmount; }
    public void setPlannedAmount(int plannedAmount) { this.plannedAmount = plannedAmount; }

    public int getActualAmount() { return actualAmount; }
    public void setActualAmount(int actualAmount) { this.actualAmount = actualAmount; }

    public String getProductionDate() { return productionDate; }
    public void setProductionDate(String productionDate) { this.productionDate = productionDate; }

    public Integer getCheckResult() { return checkResult; }
    public void setCheckResult(Integer checkResult) { this.checkResult = checkResult; }

    public Integer getCodeStatus() { return codeStatus; }
    public void setCodeStatus(Integer codeStatus) { this.codeStatus = codeStatus; }

    public Integer getBatchStatus() { return batchStatus; }
    public void setBatchStatus(Integer batchStatus) { this.batchStatus = batchStatus; }

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

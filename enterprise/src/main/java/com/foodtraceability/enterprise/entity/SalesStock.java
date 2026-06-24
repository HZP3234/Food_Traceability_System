package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_sales_stock")
public class SalesStock {
    @TableId(type = IdType.AUTO)
    private Long stockId;
    private String stockCode;
    private String terminalId;
    private String terminalName;
    private String prodBatchId;
    private String prodBatchNo;
    private String productName;
    private int quantity;
    private String receivedTime;
    private String lastCheckTime;
    private int stockStatus;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Long getStockId() { return stockId; }
    public void setStockId(Long stockId) { this.stockId = stockId; }

    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }

    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }

    public String getTerminalName() { return terminalName; }
    public void setTerminalName(String terminalName) { this.terminalName = terminalName; }

    public String getProdBatchId() { return prodBatchId; }
    public void setProdBatchId(String prodBatchId) { this.prodBatchId = prodBatchId; }

    public String getProdBatchNo() { return prodBatchNo; }
    public void setProdBatchNo(String prodBatchNo) { this.prodBatchNo = prodBatchNo; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getReceivedTime() { return receivedTime; }
    public void setReceivedTime(String receivedTime) { this.receivedTime = receivedTime; }

    public String getLastCheckTime() { return lastCheckTime; }
    public void setLastCheckTime(String lastCheckTime) { this.lastCheckTime = lastCheckTime; }

    public int getStockStatus() { return stockStatus; }
    public void setStockStatus(int stockStatus) { this.stockStatus = stockStatus; }

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

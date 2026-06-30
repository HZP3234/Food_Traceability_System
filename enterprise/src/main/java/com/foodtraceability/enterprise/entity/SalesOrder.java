package com.foodtraceability.enterprise.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_sales_order")
public class SalesOrder {
    @TableId(type = IdType.AUTO)
    private Long salesOrderId;
    private String salesOrderCode;
    private String productName;
    private String prodBatchNo;
    private String buyerEnterpriseId;
    private String buyerEnterpriseName;
    private String sellerEnterpriseName;
    private String terminalId;
    private String terminalName;
    private Integer orderQuantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String orderDate;
    private Integer orderStatus;
    private String detailId;
    private Integer detailStatus;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private Integer isDeleted;

    public Long getSalesOrderId() { return salesOrderId; }
    public void setSalesOrderId(Long salesOrderId) { this.salesOrderId = salesOrderId; }

    public String getSalesOrderCode() { return salesOrderCode; }
    public void setSalesOrderCode(String salesOrderCode) { this.salesOrderCode = salesOrderCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProdBatchNo() { return prodBatchNo; }
    public void setProdBatchNo(String prodBatchNo) { this.prodBatchNo = prodBatchNo; }

    public String getBuyerEnterpriseId() { return buyerEnterpriseId; }
    public void setBuyerEnterpriseId(String buyerEnterpriseId) { this.buyerEnterpriseId = buyerEnterpriseId; }

    public String getBuyerEnterpriseName() { return buyerEnterpriseName; }
    public void setBuyerEnterpriseName(String buyerEnterpriseName) { this.buyerEnterpriseName = buyerEnterpriseName; }

    public String getSellerEnterpriseName() { return sellerEnterpriseName; }
    public void setSellerEnterpriseName(String sellerEnterpriseName) { this.sellerEnterpriseName = sellerEnterpriseName; }

    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }

    public String getTerminalName() { return terminalName; }
    public void setTerminalName(String terminalName) { this.terminalName = terminalName; }

    public Integer getOrderQuantity() { return orderQuantity; }
    public void setOrderQuantity(Integer orderQuantity) { this.orderQuantity = orderQuantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public Integer getOrderStatus() { return orderStatus; }
    public void setOrderStatus(Integer orderStatus) { this.orderStatus = orderStatus; }

    public String getDetailId() { return detailId; }
    public void setDetailId(String detailId) { this.detailId = detailId; }

    public Integer getDetailStatus() { return detailStatus; }
    public void setDetailStatus(Integer detailStatus) { this.detailStatus = detailStatus; }

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

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}

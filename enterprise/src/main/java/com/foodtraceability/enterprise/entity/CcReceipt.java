package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
//
@TableName("t_cc_receipt")
public class CcReceipt {
    @TableId(type = IdType.AUTO)
    private Long receiptId;
    private String orderNo;
    private String signer;
    private String signTime;
    private String signTemp;
    private int isPackageIntact;
    private int qtyMatch;
    private String qtyMismatchDesc;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Long getReceiptId() { return receiptId; }
    public void setReceiptId(Long receiptId) { this.receiptId = receiptId; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getSigner() { return signer; }
    public void setSigner(String signer) { this.signer = signer; }

    public String getSignTime() { return signTime; }
    public void setSignTime(String signTime) { this.signTime = signTime; }

    public String getSignTemp() { return signTemp; }
    public void setSignTemp(String signTemp) { this.signTemp = signTemp; }

    public int getIsPackageIntact() { return isPackageIntact; }
    public void setIsPackageIntact(int isPackageIntact) { this.isPackageIntact = isPackageIntact; }

    public int getQtyMatch() { return qtyMatch; }
    public void setQtyMatch(int qtyMatch) { this.qtyMatch = qtyMatch; }

    public String getQtyMismatchDesc() { return qtyMismatchDesc; }
    public void setQtyMismatchDesc(String qtyMismatchDesc) { this.qtyMismatchDesc = qtyMismatchDesc; }

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

package com.foodtraceability.enterprise.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_sales_terminal")
public class SalesTerminal {
    @TableId(type = IdType.AUTO)
    private Integer terminalId;
    private String terminalCode;
    private String terminalName;
    private int terminalType;
    private String area;
    private String address;
    private String operatorId;
    private String operatorName;
    private Integer terminalStatus;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Integer getTerminalId() { return terminalId; }
    public void setTerminalId(Integer terminalId) { this.terminalId = terminalId; }

    public String getTerminalCode() { return terminalCode; }
    public void setTerminalCode(String terminalCode) { this.terminalCode = terminalCode; }

    public String getTerminalName() { return terminalName; }
    public void setTerminalName(String terminalName) { this.terminalName = terminalName; }

    public int getTerminalType() { return terminalType; }
    public void setTerminalType(int terminalType) { this.terminalType = terminalType; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public Integer getTerminalStatus() { return terminalStatus; }
    public void setTerminalStatus(Integer terminalStatus) { this.terminalStatus = terminalStatus; }

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

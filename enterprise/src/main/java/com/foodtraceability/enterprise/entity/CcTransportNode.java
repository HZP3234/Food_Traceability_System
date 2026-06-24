package com.foodtraceability.enterprise.entity;
//
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_cc_transport_node")
public class CcTransportNode {
    @TableId(type = IdType.AUTO)
    private Long nodeId;
    private String orderNo;
    private int nodeType;
    private String nodeTime;
    private String location;
    private String operator;
    private int nodeStatus;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private int isDeleted;

    public Long getNodeId() { return nodeId; }
    public void setNodeId(Long nodeId) { this.nodeId = nodeId; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public int getNodeType() { return nodeType; }
    public void setNodeType(int nodeType) { this.nodeType = nodeType; }

    public String getNodeTime() { return nodeTime; }
    public void setNodeTime(String nodeTime) { this.nodeTime = nodeTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public int getNodeStatus() { return nodeStatus; }
    public void setNodeStatus(int nodeStatus) { this.nodeStatus = nodeStatus; }

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

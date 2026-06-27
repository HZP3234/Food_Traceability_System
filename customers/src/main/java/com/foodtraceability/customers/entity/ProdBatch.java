package com.foodtraceability.customers.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("t_prod_batch")
public class ProdBatch implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long prodBatchId;

    private String batchNo;

    private String productName;

    private String processBatchNo;

    private String rawBatchNo;

    private String productionLine;

    private Integer plannedAmount;

    private Integer actualAmount;

    private String productionDate;

    private Integer checkResult;

    private Integer codeStatus;

    private Integer batchStatus;

    private String dataHash;

    private String chainHash;

    private String remark;

    private String createBy;

    private String createTime;

    private String updateBy;

    private String updateTime;

    private Integer isDeleted;
}

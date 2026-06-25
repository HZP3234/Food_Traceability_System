package com.foodtraceability.customers.dto;

import lombok.Data;

@Data
public class ComplaintQueryDTO {

    private String complaintNo;

    private String productBatchNo;

    private String productName;

    private String consumerPhone;

    private Long consumerId;

    private Integer complaintType;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}

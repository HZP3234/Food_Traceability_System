package com.foodtraceability.customers.dto;

import lombok.Data;

@Data
public class ComplaintQueryDTO {

    private String complaintNo;

    private String traceCode;

    private String batchNumber;

    private String enterpriseName;

    private String phone;

    private String consumerUuid;

    private Integer complaintType;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}

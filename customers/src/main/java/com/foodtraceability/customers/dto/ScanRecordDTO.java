package com.foodtraceability.customers.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScanRecordDTO {

    @NotBlank(message = "产品批次号不能为空")
    private String productBatchNo;

    private String scanIp;

    private String scanLocation;
}

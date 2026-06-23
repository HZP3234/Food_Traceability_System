package com.foodtraceability.customers.dto;

import com.foodtraceability.customers.entity.TraceabilityNode;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TraceabilityVO {

    private String productBatchNo;

    private String productName;

    private String productSpec;

    private String manufacturer;

    private String origin;

    private LocalDate productionDate;

    private LocalDate expirationDate;

    private List<TraceabilityNode> nodes;
}

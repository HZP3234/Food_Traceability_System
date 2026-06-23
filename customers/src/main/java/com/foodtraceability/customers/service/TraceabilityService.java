package com.foodtraceability.customers.service;

import com.foodtraceability.customers.dto.TraceabilityVO;

public interface TraceabilityService {

    TraceabilityVO queryByBatchNo(String productBatchNo);

    void recordScan(String productBatchNo, String scanIp, String scanLocation);
}

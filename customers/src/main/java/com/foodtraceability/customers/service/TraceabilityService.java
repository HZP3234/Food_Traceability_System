package com.foodtraceability.customers.service;

import com.foodtraceability.customers.dto.TraceabilityVO;
import com.foodtraceability.customers.entity.ScanRecord;

import java.util.List;

public interface TraceabilityService {

    TraceabilityVO queryByBatchNo(String productBatchNo);

    TraceabilityVO queryByTraceCode(String traceCode);

    void recordScan(String productBatchNo, String scanIp, String scanLocation, String userId);

    List<ScanRecord> getRecentScans(int limit);

    List<ScanRecord> getScansByUserId(String userId, int limit);
}

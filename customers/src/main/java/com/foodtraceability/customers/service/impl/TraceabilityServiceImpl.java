package com.foodtraceability.customers.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.foodtraceability.customers.dto.TraceabilityVO;
import com.foodtraceability.customers.entity.ProductTraceability;
import com.foodtraceability.customers.entity.ScanRecord;
import com.foodtraceability.customers.entity.TraceabilityNode;
import com.foodtraceability.customers.mapper.ProductTraceabilityMapper;
import com.foodtraceability.customers.mapper.ScanRecordMapper;
import com.foodtraceability.customers.mapper.TraceabilityNodeMapper;
import com.foodtraceability.customers.service.TraceabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TraceabilityServiceImpl implements TraceabilityService {

    private final ProductTraceabilityMapper productTraceabilityMapper;
    private final TraceabilityNodeMapper traceabilityNodeMapper;
    private final ScanRecordMapper scanRecordMapper;

    @Override
    public TraceabilityVO queryByBatchNo(String productBatchNo) {
        LambdaQueryWrapper<ProductTraceability> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(ProductTraceability::getProductBatchNo, productBatchNo);
        ProductTraceability product = productTraceabilityMapper.selectOne(productWrapper);
        if (product == null) {
            throw new RuntimeException("未找到该批次产品的溯源信息");
        }

        LambdaQueryWrapper<TraceabilityNode> nodeWrapper = new LambdaQueryWrapper<>();
        nodeWrapper.eq(TraceabilityNode::getProductBatchNo, productBatchNo);
        nodeWrapper.orderByAsc(TraceabilityNode::getSortOrder);
        List<TraceabilityNode> nodes = traceabilityNodeMapper.selectList(nodeWrapper);

        TraceabilityVO vo = new TraceabilityVO();
        vo.setProductBatchNo(product.getProductBatchNo());
        vo.setProductName(product.getProductName());
        vo.setProductSpec(product.getProductSpec());
        vo.setManufacturer(product.getManufacturer());
        vo.setOrigin(product.getOrigin());
        vo.setProductionDate(product.getProductionDate());
        vo.setExpirationDate(product.getExpirationDate());
        vo.setNodes(nodes);
        return vo;
    }

    @Override
    @Transactional
    public void recordScan(String productBatchNo, String scanIp, String scanLocation) {
        ScanRecord record = new ScanRecord();
        record.setProductBatchNo(productBatchNo);
        record.setScanIp(scanIp);
        record.setScanLocation(scanLocation);
        record.setScanTime(LocalDateTime.now());
        scanRecordMapper.insert(record);
    }
}

package com.foodtraceability.customers.service.impl;

import com.foodtraceability.customers.common.BusinessException;
import com.foodtraceability.customers.dto.TraceabilityVO;
import com.foodtraceability.customers.entity.ProdBatch;
import com.foodtraceability.customers.entity.ScanRecord;
import com.foodtraceability.customers.entity.TraceabilityNode;
import com.foodtraceability.customers.entity.TraceCode;
import com.foodtraceability.customers.mapper.*;
import com.foodtraceability.customers.service.TraceabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TraceabilityServiceImpl implements TraceabilityService {

    private final ProdBatchMapper prodBatchMapper;
    private final ProcessBatchMapper processBatchMapper;
    private final QualityInspectionMapper qualityInspectionMapper;
    private final CcTransportMapper ccTransportMapper;
    private final TraceCodeMapper traceCodeMapper;
    private final ScanRecordMapper scanRecordMapper;
    private final ConsumerMapper consumerMapper;

    @Override
    public TraceabilityVO queryByBatchNo(String productBatchNo) {
        return buildTraceabilityVO(productBatchNo, null);
    }

    @Override
    public TraceabilityVO queryByTraceCode(String traceCode) {
        TraceCode tc = traceCodeMapper.selectByTraceCode(traceCode);
        if (tc == null) {
            throw BusinessException.notFound("该商品没有溯源信息或溯源码被禁用");
        }
        if (tc.getTraceCodeStatus() != null && tc.getTraceCodeStatus() >= 3) {
            String tip = tc.getTraceCodeStatus() == 3 ? "该溯源码已被禁用" :
                         tc.getTraceCodeStatus() == 4 ? "该溯源码已作废" : "该溯源码已过期";
            throw BusinessException.badRequest(tip);
        }

        String batchNo = tc.getBatchNo();
        if (batchNo == null || batchNo.isBlank()) {
            throw BusinessException.notFound("该商品没有溯源信息或溯源码被禁用");
        }

        return buildTraceabilityVO(batchNo, tc);
    }

    private TraceabilityVO buildTraceabilityVO(String batchNo, TraceCode tc) {
        TraceabilityVO vo = new TraceabilityVO();
        vo.setProductBatchNo(batchNo);

        if (tc != null) {
            vo.setTraceCode(tc.getTraceCode());
        }

        ProdBatch prod = prodBatchMapper.selectByBatchNo(batchNo);

        if (tc != null) {
            vo.setProductName(tc.getProductName());
            vo.setManufacturer(tc.getEnterpriseName());
            vo.setTxHash(tc.getTxHash());
        }

        if (prod != null) {
            if (vo.getProductName() == null || vo.getProductName().isBlank()) {
                vo.setProductName(prod.getProductName());
            }
            vo.setProductionDate(prod.getProductionDate());
            vo.setProductionLine(prod.getProductionLine());
            vo.setCheckResult(prod.getCheckResult());
            if (vo.getTxHash() == null) {
                vo.setTxHash(prod.getChainHash());
            }
        }

        if (prod == null && tc == null) {
            throw BusinessException.notFound("该商品没有溯源信息或溯源码被禁用");
        }

        vo.setNodes(buildNodes(batchNo, prod));
        return vo;
    }

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    private List<TraceabilityNode> buildNodes(String batchNo, ProdBatch prod) {
        List<TraceabilityNode> nodes = new ArrayList<>();
        int sort = 0;

        if (prod != null && prod.getProcessBatchNo() != null) {
            Map<String, Object> proc = processBatchMapper.selectByBatchNo(prod.getProcessBatchNo());
            if (proc != null) {
                sort++;
                TraceabilityNode node = new TraceabilityNode();
                node.setNodeName("加工");
                node.setSortOrder(sort);
                node.setNodeTime(parseDateTime(str(proc.get("process_date"))));
                node.setOperator(str(proc.get("operator")));
                node.setNodeDescription(String.format("工艺: %s，温度: %s℃，时长: %s",
                        str(proc.get("template_name")),
                        str(proc.get("actual_temp")),
                        str(proc.get("actual_duration"))));
                nodes.add(node);
            }
        }

        if (prod != null) {
            sort++;
            TraceabilityNode node = new TraceabilityNode();
            node.setNodeName("生产");
            node.setSortOrder(sort);
            node.setNodeTime(parseDateTime(prod.getProductionDate()));
            node.setLocation(prod.getProductionLine());
            node.setOperator(prod.getCreateBy());
            node.setNodeDescription(String.format("产品: %s，计划量: %d，实际量: %d",
                    prod.getProductName(), prod.getPlannedAmount(), prod.getActualAmount()));
            nodes.add(node);
        }

        List<Map<String, Object>> inspections = qualityInspectionMapper.selectByBizBatchNo(batchNo);
        for (Map<String, Object> insp : inspections) {
            sort++;
            int result = toInt(insp.get("inspection_result"));
            TraceabilityNode node = new TraceabilityNode();
            node.setNodeName("质检");
            node.setSortOrder(sort);
            node.setNodeTime(parseDateTime(str(insp.get("inspection_date"))));
            node.setOperator(str(insp.get("inspector")));
            node.setNodeDescription(String.format("标准: %s，结果: %s，感官: %s，微生物: %s，密封: %s",
                    str(insp.get("standard")),
                    result == 1 ? "合格" : result == 2 ? "不合格" : "待定",
                    toInt(insp.get("sensory_check")) == 1 ? "合格" : "异常",
                    toInt(insp.get("microbe_check")) == 1 ? "合格" : "异常",
                    str(insp.get("seal_check"))));
            nodes.add(node);
        }

        List<Map<String, Object>> transports = ccTransportMapper.selectByProdBatchNo(batchNo);
        for (Map<String, Object> trans : transports) {
            sort++;
            int status = toInt(trans.get("transport_status"));
            String statusName = status == 1 ? "运输中" : status == 2 ? "已到达" : status == 3 ? "已完成" : "未知";
            TraceabilityNode node = new TraceabilityNode();
            node.setNodeName("物流");
            node.setSortOrder(sort);
            node.setNodeTime(parseDateTime(str(trans.get("depart_time"))));
            node.setLocation(str(trans.get("departure_name")) + " → " + str(trans.get("destination_name")));
            node.setOperator(str(trans.get("driver_name")));
            node.setNodeDescription(String.format("运单: %s，车牌: %s，状态: %s，温控: %s~%s℃",
                    str(trans.get("order_no")), str(trans.get("plate_no")),
                    statusName,
                    str(trans.get("temp_lower")), str(trans.get("temp_upper"))));
            nodes.add(node);
        }

        return nodes;
    }

    @Override
    @Transactional
    public void recordScan(String productBatchNo, String scanIp, String scanLocation, String userId, String traceCode) {
        ScanRecord record = new ScanRecord();
        record.setProductBatchNo(productBatchNo);
        record.setTraceCode(traceCode);
        record.setScanIp(scanIp);
        record.setScanLocation(scanLocation);
        record.setUserId(userId);
        record.setScanTime(LocalDateTime.now());
        scanRecordMapper.insert(record);

        if (userId != null && !userId.isBlank()) {
            consumerMapper.incrementScanCount(userId);
        }
    }

    private static LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                if (fmt == DATE_FORMATS[2]) {
                    return LocalDate.parse(s, fmt).atStartOfDay();
                }
                return LocalDateTime.parse(s.replace(" ", "T"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private static String str(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    private static int toInt(Object obj) {
        if (obj instanceof Number) return ((Number) obj).intValue();
        if (obj instanceof String) {
            try { return Integer.parseInt((String) obj); } catch (Exception ignored) {}
        }
        return 0;
    }
}

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraceabilityServiceImpl implements TraceabilityService {

    private final ProdBatchMapper prodBatchMapper;
    private final CustRawMapper custRawMapper;
    private final CcTransportMapper ccTransportMapper;
    private final CustSalesStockMapper custSalesStockMapper;
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

        // 确定商品名称：优先从溯源码取，其次从生产批次取
        String productName = null;
        if (tc != null) {
            vo.setProductName(tc.getProductName());
            vo.setManufacturer(tc.getEnterpriseName());
            vo.setTxHash(tc.getTxHash());
            productName = tc.getProductName();
        }
        if (prod != null) {
            if (productName == null || productName.isBlank()) {
                productName = prod.getProductName();
                vo.setProductName(productName);
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

        // 通过商品名称检索原料、物流、销售信息
        List<Map<String, Object>> rawList = new ArrayList<>();
        List<Map<String, Object>> transportList = new ArrayList<>();
        List<Map<String, Object>> salesList = new ArrayList<>();

        if (productName != null && !productName.isBlank()) {
            try {
                rawList = custRawMapper.selectByProductName(productName);
            } catch (Exception e) {
                log.warn("查询原料失败: productName={}", productName, e);
            }
            transportList = ccTransportMapper.selectByProductName(productName, batchNo);
            try {
                salesList = custSalesStockMapper.selectByProductName(productName, batchNo);
            } catch (Exception e) {
                log.warn("查询销售库存失败: productName={}", productName, e);
            }

            if (!rawList.isEmpty()) {
                Map<String, Object> firstRaw = rawList.get(0);
                vo.setSupplierName(str(firstRaw.get("supplier_name")));
                vo.setRawBatchNo(str(firstRaw.get("batch_no")));
                if (vo.getManufacturer() == null || vo.getManufacturer().isBlank()) {
                    vo.setManufacturer(str(firstRaw.get("supplier_name")));
                }
            }
        }

        vo.setNodes(buildNodes(batchNo, prod, rawList, transportList, salesList));
        return vo;
    }

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    private List<TraceabilityNode> buildNodes(String batchNo, ProdBatch prod,
            List<Map<String, Object>> rawList,
            List<Map<String, Object>> transportList,
            List<Map<String, Object>> salesList) {
        List<TraceabilityNode> nodes = new ArrayList<>();
        int sort = 0;

        // ========== 1. 原料 ==========
        for (Map<String, Object> raw : rawList) {
            sort++;
            int checkResult = toInt(raw.get("check_result"));
            String rawCheckTxt = checkResult == 1 ? "合格" : checkResult == 2 ? "不合格" : "未检测";
            TraceabilityNode node = new TraceabilityNode();
            node.setNodeName("原料");
            node.setSortOrder(sort);
            node.setNodeTime(parseDateTime(str(raw.get("purchase_date"))));
            node.setLocation(str(raw.get("warehouse")));
            node.setOperator(str(raw.get("supplier_name")));
            node.setNodeDescription("原料批次: " + str(raw.get("batch_no"))
                    + "，原料: " + str(raw.get("product_name"))
                    + "，数量: " + str(raw.get("amount")) + str(raw.get("unit"))
                    + "，质检: " + rawCheckTxt
                    + "，保质期: " + str(raw.get("shelf_life")));
            nodes.add(node);
        }

        // ========== 2. 生产 ==========
        if (prod != null) {
            sort++;
            int planned = prod.getPlannedAmount() != null ? prod.getPlannedAmount() : 0;
            int actual = prod.getActualAmount() != null ? prod.getActualAmount() : 0;
            String prodDate = prod.getProductionDate() != null ? prod.getProductionDate() : "";
            TraceabilityNode node = new TraceabilityNode();
            node.setNodeName("生产");
            node.setSortOrder(sort);
            node.setNodeTime(parseDateTime(prod.getProductionDate()));
            node.setLocation(prod.getProductionLine());
            node.setOperator(prod.getCreateBy());
            node.setNodeDescription("产品: " + prod.getProductName()
                    + "，生产批次: " + batchNo
                    + "，生产线: " + prod.getProductionLine()
                    + "，生产日期: " + prodDate
                    + "，计划产量: " + planned + "，实际产量: " + actual);
            nodes.add(node);
        }

        // ========== 3. 冷链物流 ==========
        for (Map<String, Object> trans : transportList) {
            sort++;
            int status = toInt(trans.get("transport_status"));
            String statusName = status == 1 ? "运输中" : status == 2 ? "已到达" : status == 3 ? "已完成" : "未知";
            TraceabilityNode node = new TraceabilityNode();
            node.setNodeName("冷链物流");
            node.setSortOrder(sort);
            node.setNodeTime(parseDateTime(str(trans.get("depart_time"))));
            node.setLocation(str(trans.get("departure_name")) + " → " + str(trans.get("destination_name")));
            node.setOperator(str(trans.get("driver_name")));
            node.setNodeDescription("运单: " + str(trans.get("order_no"))
                    + "，车牌: " + str(trans.get("plate_no"))
                    + "，状态: " + statusName
                    + "，温控: " + str(trans.get("temp_lower")) + "~" + str(trans.get("temp_upper")) + "℃");
            nodes.add(node);
        }

        // ========== 4. 销售 ==========
        for (Map<String, Object> stock : salesList) {
            sort++;
            int stockStatus = toInt(stock.get("stock_status"));
            String statusName = stockStatus == 0 ? "在库" : stockStatus == 1 ? "已上架" :
                                stockStatus == 2 ? "已售罄" : "未知";
            TraceabilityNode node = new TraceabilityNode();
            node.setNodeName("销售");
            node.setSortOrder(sort);
            node.setNodeTime(parseDateTime(str(stock.get("received_time"))));
            node.setLocation(str(stock.get("terminal_name")));
            node.setOperator(str(stock.get("stock_code")));
            node.setNodeDescription("产品: " + str(stock.get("product_name"))
                    + "，入库数量: " + toInt(stock.get("quantity"))
                    + "，终端: " + str(stock.get("terminal_name"))
                    + "，状态: " + statusName);
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

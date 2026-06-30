package com.foodtraceability.customers.service.impl;

import com.foodtraceability.customers.common.BusinessException;
import com.foodtraceability.customers.dto.TraceabilityVO;
import com.foodtraceability.customers.entity.Enterprise;
import com.foodtraceability.customers.entity.ProdBatch;
import com.foodtraceability.customers.entity.ScanRecord;
import com.foodtraceability.customers.entity.TraceabilityNode;
import com.foodtraceability.customers.entity.TraceCode;
import com.foodtraceability.customers.mapper.ConsumerMapper;
import com.foodtraceability.customers.mapper.CustEnterpriseMapper;
import com.foodtraceability.customers.mapper.CustRawMapper;
import com.foodtraceability.customers.mapper.CustSalesOrderMapper;
import com.foodtraceability.customers.mapper.CustSalesStockMapper;
import com.foodtraceability.customers.mapper.CcTransportMapper;
import com.foodtraceability.customers.mapper.ProcessBatchMapper;
import com.foodtraceability.customers.mapper.ProdBatchMapper;
import com.foodtraceability.customers.mapper.ScanRecordMapper;
import com.foodtraceability.customers.mapper.TraceCodeMapper;
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
    private final CustSalesOrderMapper custSalesOrderMapper;
    private final ProcessBatchMapper processBatchMapper;
    private final TraceCodeMapper traceCodeMapper;
    private final ScanRecordMapper scanRecordMapper;
    private final ConsumerMapper consumerMapper;
    private final CustEnterpriseMapper custEnterpriseMapper;

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

        // 确定商品名称：从溯源码取（权威来源）
        String productName = null;
        if (tc != null) {
            vo.setProductName(tc.getProductName());
            vo.setManufacturer(tc.getEnterpriseName());
            vo.setTxHash(tc.getTxHash());
            productName = tc.getProductName();

            // 查询企业详细信息
            if (tc.getEnterpriseUuid() != null && !tc.getEnterpriseUuid().isBlank()) {
                try {
                    Enterprise enterprise = custEnterpriseMapper.selectByEnterpriseUuid(tc.getEnterpriseUuid());
                    if (enterprise != null) {
                        vo.setEnterpriseType(enterprise.getEnterpriseType());
                        vo.setCertNo(enterprise.getCertNo());
                        vo.setAddress(enterprise.getAddress());
                        vo.setContactPhone(enterprise.getContactPhone());
                        vo.setContactPerson(enterprise.getContactPerson());
                    }
                } catch (Exception e) {
                    log.warn("查询企业信息失败: enterpriseUuid={}", tc.getEnterpriseUuid(), e);
                }
            }
        }

        // 校验 ProdBatch 的产品名是否与溯源码一致，不一致则按productName重查
        if (prod != null && productName != null && !productName.isBlank()
                && !productName.equals(prod.getProductName())) {
            log.warn("ProdBatch产品名不匹配，改用productName重查: traceCodeProduct={}, prodBatchProduct={}",
                    productName, prod.getProductName());
            prod = prodBatchMapper.selectByProductName(productName);
        }

        if (prod != null) {
            if (productName == null || productName.isBlank()) {
                productName = prod.getProductName();
                vo.setProductName(productName);
            }
            vo.setProductionDate(prod.getProductionDate());
            vo.setProductionLine(prod.getProductionLine());
            if (vo.getTxHash() == null) {
                vo.setTxHash(prod.getChainHash());
            }
        }

        if (prod == null && tc == null) {
            throw BusinessException.notFound("该商品没有溯源信息或溯源码被禁用");
        }

        // 通过商品名称检索原料、生产、物流、销售信息
        List<Map<String, Object>> rawList = new ArrayList<>();
        List<Map<String, Object>> processList = new ArrayList<>();
        List<Map<String, Object>> transportList = new ArrayList<>();
        List<Map<String, Object>> salesStockList = new ArrayList<>();
        List<Map<String, Object>> salesOrderList = new ArrayList<>();

        if (productName != null && !productName.isBlank()) {
            // 1. 原料 - 优先用 ProdBatch.rawBatchNo 精确查询
            try {
                String rawBatchNoFromProd = prod != null ? prod.getRawBatchNo() : null;
                if (rawBatchNoFromProd != null && !rawBatchNoFromProd.isBlank()) {
                    Map<String, Object> rawByBatch = custRawMapper.selectByBatchNo(rawBatchNoFromProd);
                    if (rawByBatch != null && !rawByBatch.isEmpty()) {
                        rawList.add(rawByBatch);
                    }
                }
                // 如果 rawBatchNo 查不到，回退到按商品名称查询
                if (rawList.isEmpty()) {
                    rawList = custRawMapper.selectByProductName(productName);
                }
            } catch (Exception e) {
                log.warn("查询原料失败: productName={}", productName, e);
            }

            // 2. 生产 - 按商品名称查 t_process_batch
            try {
                processList = processBatchMapper.selectByProductName(productName);
            } catch (Exception e) {
                log.warn("查询加工批次失败: productName={}", productName, e);
            }

            // 3. 物流 - 按商品名称+批次号查 t_cc_transport（成品运输 + 原料运输）
            transportList = ccTransportMapper.selectByProductName(productName, batchNo);
            // 同时按原料批次号查原料运输订单
            try {
                List<Map<String, Object>> rawTransports = ccTransportMapper.selectByRawBatchNo(batchNo);
                if (rawTransports != null && !rawTransports.isEmpty()) {
                    // 合并去重（按 order_no 去重）
                    java.util.Set<String> existingOrderNos = new java.util.HashSet<>();
                    for (Map<String, Object> t : transportList) {
                        Object on = t.get("order_no");
                        if (on != null) existingOrderNos.add(on.toString());
                    }
                    for (Map<String, Object> rt : rawTransports) {
                        Object on = rt.get("order_no");
                        if (on != null && !existingOrderNos.contains(on.toString())) {
                            transportList.add(rt);
                            existingOrderNos.add(on.toString());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("查询原料运输订单失败: batchNo={}", batchNo, e);
            }

            // 4. 销售 - 库存信息
            try {
                salesStockList = custSalesStockMapper.selectByProductName(productName, batchNo);
            } catch (Exception e) {
                log.warn("查询销售库存失败: productName={}", productName, e);
            }

            // 4. 销售 - 订单信息
            try {
                salesOrderList = custSalesOrderMapper.selectByProductName(productName, batchNo);
            } catch (Exception e) {
                log.warn("查询销售订单失败: productName={}", productName, e);
            }

            if (!rawList.isEmpty()) {
                Map<String, Object> firstRaw = rawList.get(0);
                vo.setSupplierName(str(firstRaw.get("supplier_name")));
                vo.setRawBatchNo(str(firstRaw.get("batch_no")));
            }
        }

        vo.setNodes(buildNodes(batchNo, prod, rawList, processList, transportList, salesStockList, salesOrderList));
        return vo;
    }

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    private List<TraceabilityNode> buildNodes(String batchNo, ProdBatch prod,
            List<Map<String, Object>> rawList,
            List<Map<String, Object>> processList,
            List<Map<String, Object>> transportList,
            List<Map<String, Object>> salesStockList,
            List<Map<String, Object>> salesOrderList) {
        List<TraceabilityNode> nodes = new ArrayList<>();
        int sort = 0;

        // ========== 1. 原料 ==========
        for (Map<String, Object> raw : rawList) {
            sort++;
            TraceabilityNode node = new TraceabilityNode();
            node.setNodeName("原料");
            node.setSortOrder(sort);
            node.setNodeTime(parseDateTime(str(raw.get("purchase_date"))));
            node.setLocation(str(raw.get("warehouse")));
            node.setOperator(str(raw.get("supplier_name")));
            node.setNodeDescription("原料批次: " + str(raw.get("batch_no"))
                    + "，原料: " + str(raw.get("product_name"))
                    + "，数量: " + str(raw.get("amount")) + str(raw.get("unit"))
                    + "，保质期: " + str(raw.get("shelf_life")));
            nodes.add(node);
        }

        // ========== 2. 生产 ==========
        // 2a. 生产批次汇总
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

        // 2b. 加工批次详情（按商品名称查到的）
        for (Map<String, Object> process : processList) {
            sort++;
            String procBatchNo = str(process.get("batch_no"));
            // 跳过与生产批次号相同的重复记录
            if (procBatchNo.equals(batchNo) || procBatchNo.equals(prod != null ? prod.getProcessBatchNo() : null)) {
                continue;
            }
            TraceabilityNode node = new TraceabilityNode();
            node.setNodeName("生产");
            node.setSortOrder(sort);
            node.setNodeTime(parseDateTime(str(process.get("process_date"))));
            node.setLocation(str(process.get("production_line")));
            node.setOperator(str(process.get("operator")));
            node.setNodeDescription("加工批次: " + procBatchNo
                    + "，模板: " + str(process.get("template_name"))
                    + "，计划产量: " + toInt(process.get("planned_amount"))
                    + "，温度: " + str(process.get("actual_temp")) + "℃"
                    + "，耗时: " + str(process.get("actual_duration"))
                    + "，状态: " + (toInt(process.get("batch_status")) == 1 ? "进行中" : toInt(process.get("batch_status")) == 2 ? "已完成" : "待开始"));
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
                    + "，状态: " + statusName);
            nodes.add(node);
        }

        // ========== 4. 销售 ==========
        // 4a. 库存信息
        for (Map<String, Object> stock : salesStockList) {
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

        // 4b. 销售订单
        for (Map<String, Object> order : salesOrderList) {
            sort++;
            int orderStatus = toInt(order.get("order_status"));
            String statusName = orderStatus == 0 ? "待发货" : orderStatus == 1 ? "已发货" :
                                orderStatus == 2 ? "已签收" : orderStatus == 3 ? "已完成" : "已取消";
            TraceabilityNode node = new TraceabilityNode();
            node.setNodeName("销售");
            node.setSortOrder(sort);
            node.setNodeTime(parseDateTime(str(order.get("order_date"))));
            node.setLocation(str(order.get("buyer_enterprise_name")));
            node.setOperator(str(order.get("seller_enterprise_name")));
            node.setNodeDescription("订单: " + str(order.get("sales_order_code"))
                    + "，买方: " + str(order.get("buyer_enterprise_name"))
                    + "，数量: " + toInt(order.get("order_quantity"))
                    + "，单价: ¥" + str(order.get("unit_price"))
                    + "，总金额: ¥" + str(order.get("total_amount"))
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

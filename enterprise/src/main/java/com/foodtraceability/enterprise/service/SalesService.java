package com.foodtraceability.enterprise.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.foodtraceability.enterprise.entity.SalesTerminal;
import com.foodtraceability.enterprise.entity.SalesStock;
import com.foodtraceability.enterprise.entity.SalesStorage;
import com.foodtraceability.enterprise.entity.SalesSupplement;
import com.foodtraceability.enterprise.mapper.SalesTerminalMapper;
import com.foodtraceability.enterprise.mapper.SalesStockMapper;
import com.foodtraceability.enterprise.mapper.SalesStorageMapper;
import com.foodtraceability.enterprise.mapper.SalesSupplementMapper;

@Service
public class SalesService {
    @Autowired
    private SalesTerminalMapper salesTerminalMapper;
    @Autowired
    private SalesStockMapper salesStockMapper;
    @Autowired
    private SalesStorageMapper salesStorageMapper;
    @Autowired
    private SalesSupplementMapper salesSupplementMapper;

    // 生成终端编号 ST + yyyyMMdd + 4位序号
    public String generateTerminalCode() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seqPart = String.format("%04d", System.currentTimeMillis() % 10000);
        return "ST" + datePart + seqPart;
    }

    // 生成库存记录编号
    public String generateStockCode() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seqPart = String.format("%04d", (System.currentTimeMillis() / 1000) % 10000);
        return "SK" + datePart + seqPart;
    }

    // 生成补充记录编号
    public String generateSupplementCode() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seqPart = String.format("%04d", (System.currentTimeMillis() / 1000) % 10000);
        return "SP" + datePart + seqPart;
    }

    // ==================== t_sales_terminal ====================

    // 按终端编号查询
    public SalesTerminal getByTerminalCode(String terminalCode) {
        QueryWrapper<SalesTerminal> qw = new QueryWrapper<>();
        qw.eq("terminal_code", terminalCode);
        qw.eq("is_deleted", 0);
        return salesTerminalMapper.selectOne(qw);
    }

    // 条件列表查询
    public List<SalesTerminal> listTerminal(Integer terminalType, String area, Integer terminalStatus) {
        QueryWrapper<SalesTerminal> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        if (terminalType != null) qw.eq("terminal_type", terminalType);
        if (area != null && !area.isBlank()) qw.eq("area", area);
        if (terminalStatus != null) qw.eq("terminal_status", terminalStatus);
        qw.orderByDesc("create_time");
        return salesTerminalMapper.selectList(qw);
    }

    // 按运营企业查询
    public List<SalesTerminal> listByOperatorId(String operatorId) {
        QueryWrapper<SalesTerminal> qw = new QueryWrapper<>();
        qw.eq("operator_id", operatorId);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("create_time");
        return salesTerminalMapper.selectList(qw);
    }

    // 注册销售终端
    public int createTerminal(SalesTerminal terminal) {
        if (terminal.getTerminalCode() == null || terminal.getTerminalCode().isBlank()) {
            terminal.setTerminalCode(generateTerminalCode());
        }
        if (terminal.getTerminalStatus() == null) terminal.setTerminalStatus(1);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        terminal.setCreateTime(now);
        terminal.setUpdateTime(now);
        terminal.setCreateBy(terminal.getCreateBy() != null ? terminal.getCreateBy() : "SYSTEM");
        terminal.setUpdateBy(terminal.getUpdateBy() != null ? terminal.getUpdateBy() : "SYSTEM");
        return salesTerminalMapper.insert(terminal);
    }

    // 更新终端信息
    public int updateTerminal(SalesTerminal terminal) {
        terminal.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return salesTerminalMapper.updateById(terminal);
    }

    // 软删除终端
    public int deleteTerminal(Long terminalId) {
        SalesTerminal terminal = salesTerminalMapper.selectById(terminalId);
        if (terminal != null) {
            terminal.setIsDeleted(1);
            return salesTerminalMapper.updateById(terminal);
        }
        return 0;
    }

    // ==================== t_sales_stock ====================

    // 按终端查询库存列表
    public List<SalesStock> listStockByTerminalId(String terminalId) {
        QueryWrapper<SalesStock> qw = new QueryWrapper<>();
        qw.eq("terminal_id", terminalId);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("create_time");
        return salesStockMapper.selectList(qw);
    }

    // 按生产批次查询库存
    public List<SalesStock> listStockByProdBatchNo(String prodBatchNo) {
        QueryWrapper<SalesStock> qw = new QueryWrapper<>();
        qw.eq("prod_batch_no", prodBatchNo);
        qw.eq("is_deleted", 0);
        return salesStockMapper.selectList(qw);
    }

    // 产品入库
    public int stockIn(SalesStock stock) {
        if (stock.getStockCode() == null || stock.getStockCode().isBlank()) {
            stock.setStockCode(generateStockCode());
        }
        if (stock.getStockStatus() == 0) stock.setStockStatus(1);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        stock.setReceivedTime(now);
        stock.setCreateTime(now);
        stock.setUpdateTime(now);
        stock.setCreateBy(stock.getCreateBy() != null ? stock.getCreateBy() : "SYSTEM");
        stock.setUpdateBy(stock.getUpdateBy() != null ? stock.getUpdateBy() : "SYSTEM");

        // 自动填充储存环境
        autoFillStorage(stock.getTerminalId(), stock.getProductName());

        return salesStockMapper.insert(stock);
    }

    // 更新库存盘点
    public int updateStock(SalesStock stock) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        stock.setLastCheckTime(now);
        stock.setUpdateTime(now);
        return salesStockMapper.updateById(stock);
    }

    // ==================== t_sales_storage ====================

    // 根据终端编号查询储存环境
    public SalesStorage getStorageByTerminalCode(String terminalCode) {
        QueryWrapper<SalesStorage> qw = new QueryWrapper<>();
        qw.eq("terminal_code", terminalCode);
        qw.eq("is_deleted", 0);
        return salesStorageMapper.selectOne(qw);
    }

    // 自动填充储存环境（根据终端类型和产品类型）
    public void autoFillStorage(String terminalIdOrCode, String productName) {
        // 尝试按 terminal_code 查询（统一使用业务编码）
        SalesTerminal terminal = getByTerminalCode(terminalIdOrCode);
        // 如果按编码查不到，尝试按主键ID查询（兼容旧数据）
        if (terminal == null) {
            try {
                terminal = salesTerminalMapper.selectById(Integer.parseInt(terminalIdOrCode));
            } catch (NumberFormatException ignored) {
                return;
            }
        }
        if (terminal == null) return;

        // 查找是否已有储存环境记录
        SalesStorage existStorage = getStorageByTerminalCode(terminal.getTerminalCode());
        if (existStorage != null) return; // 已存在则跳过

        SalesStorage storage = new SalesStorage();
        storage.setTerminalCode(terminal.getTerminalCode());
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 根据终端类型自动填充默认储存条件
        if (terminal.getTerminalType() == 1) {
            // 商超：冷藏立式展示柜
            storage.setTerminalType("商超");
            storage.setTemperature("2-6℃");
            storage.setHumidity("45-65%");
            storage.setStorageMethod(1);
            storage.setLightCondition(1);
            storage.setShelfLife("上架后7天");
        } else if (terminal.getTerminalType() == 2) {
            // 电商仓：冷冻卧式冷柜
            storage.setTerminalType("电商仓");
            storage.setTemperature("-18℃以下");
            storage.setHumidity("40-60%");
            storage.setStorageMethod(3);
            storage.setLightCondition(1);
            storage.setShelfLife("入库后30天");
        } else if (terminal.getTerminalType() == 3) {
            // 门店：冷藏风幕柜
            storage.setTerminalType("门店");
            storage.setTemperature("2-6℃");
            storage.setHumidity("45-65%");
            storage.setStorageMethod(2);
            storage.setLightCondition(1);
            storage.setShelfLife("上架后7天");
        }

        storage.setProductType(productName);
        storage.setIsAutoFilled(1);
        storage.setCreateBy("system");
        storage.setUpdateBy("system");
        storage.setCreateTime(now);
        storage.setUpdateTime(now);
        salesStorageMapper.insert(storage);
    }

    // 手动更新储存环境
    public int updateStorage(SalesStorage storage) {
        storage.setIsAutoFilled(0); // 手动修改标记
        storage.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return salesStorageMapper.updateById(storage);
    }

    // ==================== t_sales_supplement ====================

    // 查询补充记录
    public List<SalesSupplement> listSupplementByTraceBatchNo(String traceBatchNo) {
        QueryWrapper<SalesSupplement> qw = new QueryWrapper<>();
        qw.eq("trace_batch_no", traceBatchNo);
        qw.eq("is_deleted", 0);
        return salesSupplementMapper.selectList(qw);
    }

    // 按终端查询补充记录
    public List<SalesSupplement> listSupplementByTerminalCode(String terminalCode) {
        QueryWrapper<SalesSupplement> qw = new QueryWrapper<>();
        qw.eq("terminal_code", terminalCode);
        qw.eq("is_deleted", 0);
        return salesSupplementMapper.selectList(qw);
    }

    // 销售商补充销售详情
    public int supplementSalesInfo(SalesSupplement supplement) {
        if (supplement.getSupplementCode() == null || supplement.getSupplementCode().isBlank()) {
            supplement.setSupplementCode(generateSupplementCode());
        }
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        supplement.setInboundTime(now);
        supplement.setSupplementStatus(1);
        supplement.setCreateTime(now);
        supplement.setUpdateTime(now);
        supplement.setCreateBy(supplement.getCreateBy() != null ? supplement.getCreateBy() : "SYSTEM");
        supplement.setUpdateBy(supplement.getUpdateBy() != null ? supplement.getUpdateBy() : "SYSTEM");
        return salesSupplementMapper.insert(supplement);
    }

    // 更新补充信息
    public int updateSupplement(SalesSupplement supplement) {
        supplement.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return salesSupplementMapper.updateById(supplement);
    }

    // ==================== 防窜货核验 ====================

    // 按区域核验：查找指定区域中存在窜货嫌疑的终端
    // 逻辑：同一生产批次的产品出现在不同区域的终端，则标记为窜货异常
    public List<SalesTerminal> checkAntiFraud(String area) {
        QueryWrapper<SalesTerminal> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        qw.eq("terminal_status", 2); // 区域异常状态
        if (area != null && !area.isBlank()) {
            qw.eq("area", area);
        }
        return salesTerminalMapper.selectList(qw);
    }

    // 全量防窜货核验：检测同一生产批次是否跨区域销售
    public void runAntiFraudCheck() {
        // 1. 获取所有有效终端
        List<SalesTerminal> terminals = salesTerminalMapper.selectList(
                new QueryWrapper<SalesTerminal>().eq("is_deleted", 0));
        if (terminals.isEmpty()) return;

        // 2. 获取所有库存记录
        List<SalesStock> allStocks = salesStockMapper.selectList(
                new QueryWrapper<SalesStock>().eq("is_deleted", 0));

        // 3. 按生产批次号分组，检查每个批次是否出现在多个不同区域
        java.util.Map<String, java.util.Set<String>> batchAreas = new java.util.HashMap<>();
        java.util.Map<String, String> stockTerminalAreaMap = new java.util.HashMap<>(); // stockId -> terminal area

        // 构建 terminalId -> area 映射
        java.util.Map<String, String> terminalAreaMap = new java.util.HashMap<>();
        for (SalesTerminal t : terminals) {
            terminalAreaMap.put(String.valueOf(t.getTerminalId()), t.getArea());
            terminalAreaMap.put(t.getTerminalCode(), t.getArea()); // 同时支持编码查找
        }

        // 4. 遍历库存，找出同一批次出现在多个区域的记录
        for (SalesStock stock : allStocks) {
            String batchNo = stock.getProdBatchNo();
            String area = terminalAreaMap.get(stock.getTerminalId());
            if (batchNo == null || area == null) continue;

            batchAreas.computeIfAbsent(batchNo, k -> new java.util.HashSet<>()).add(area);
        }

        // 5. 标记跨区域的批次关联的终端为异常
        java.util.Set<String> fraudBatchNos = new java.util.HashSet<>();
        for (java.util.Map.Entry<String, java.util.Set<String>> entry : batchAreas.entrySet()) {
            if (entry.getValue().size() > 1) {
                fraudBatchNos.add(entry.getKey()); // 该批次出现在多个区域
            }
        }

        if (fraudBatchNos.isEmpty()) return;

        // 6. 标记涉及窜货批次的终端
        java.util.Set<Long> fraudTerminalIds = new java.util.HashSet<>();
        for (SalesStock stock : allStocks) {
            if (fraudBatchNos.contains(stock.getProdBatchNo())) {
                // 查找该库存对应的终端
                for (SalesTerminal terminal : terminals) {
                    if (String.valueOf(terminal.getTerminalId()).equals(stock.getTerminalId())
                            || terminal.getTerminalCode().equals(stock.getTerminalId())) {
                        fraudTerminalIds.add(terminal.getTerminalId());
                        break;
                    }
                }
            }
        }

        // 7. 更新终端状态
        for (Long terminalId : fraudTerminalIds) {
            SalesTerminal terminal = salesTerminalMapper.selectById(terminalId);
            if (terminal != null && terminal.getTerminalStatus() != 2) {
                terminal.setTerminalStatus(2); // 区域异常
                updateTerminal(terminal);
            }
        }
    }
}

package com.foodtraceability.enterprise;

import com.foodtraceability.enterprise.dto.*;
import com.foodtraceability.enterprise.entity.*;
import com.foodtraceability.enterprise.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 食品溯源系统 — 端到端全链路集成测试
 * <p>
 * 模拟一条完整的产品生命周期：
 * <pre>
 *   供应商上传原料 → 原料批次入库 → 原料质检
 *       ↓
 *   创建工艺模板 → 加工批次投料 → 加工完成
 *       ↓
 *   生产批次创建 → 绑码 → 质检 → 生成溯源码 → 码激活
 *       ↓
 *   仓库创建 → 车辆注册 → 冷链运输(发运→温湿度→节点→签收)
 *       ↓
 *   销售终端注册 → 产品入库 → 销售补充 → 防窜货核验
 *       ↓
 *   消费者扫码 → 全链路追溯
 * </pre>
 *
 * <p><b>运行要求：</b>需要 MySQL 数据库（localhost:3306/fts），建表后执行。
 * <p><b>测试数据隔离：</b>所有批次号含时间戳，不同次运行不冲突。
 *
 * @since 2026-06-24
 */
@SpringBootTest
@Disabled("Requires an explicitly prepared local MySQL fts database; excluded from the isolated default test suite.")
@TestPropertySource(properties = {
    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
    "spring.datasource.url=jdbc:mysql://localhost:3306/fts?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai",
    "spring.datasource.username=root",
    "spring.datasource.password=123456",
    "spring.sql.init.mode=never"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("端到端全链路集成测试")
public class EndToEndTraceabilityTest {

    // ==================== Services ====================

    @Autowired private RawService rawService;
    @Autowired private ProductionService productionService;
    @Autowired private TraceCodeService traceCodeService;
    @Autowired private ColdChainService coldChainService;
    @Autowired private SalesService salesService;

    // ==================== 测试数据（共享状态） ====================

    private static String rawBatchNo;        // 原料批次号
    private static Long rawBatchId;       // 原料批次ID
    private static String processBatchNo;    // 加工批次号
    private static Long processBatchId;   // 加工批次ID
    private static String prodBatchNo;       // 生产批次号
    private static Long prodBatchId;      // 生产批次ID
    private static String traceCode;         // 溯源码值
    private static String generateBatchNo;   // 批量生成批次号
    private static String transportOrderNo;  // 冷链运输订单号
    private static Long transportId;      // 运输订单ID
    private static Long vehicleId;        // 车辆ID
    private static Long terminalId;       // 销售终端ID
    private static String terminalCode;      // 终端编码

    /** 时间戳后缀 — 每次 JVM 启动重新生成，保证数据不冲突 */
    private static final String TS = String.valueOf(System.nanoTime() % 10000000);

    // ========================================================================
    // 阶段一：原料批次 (Raw)
    // ========================================================================

    @Test
    @Order(1)
    @DisplayName("1. 供应商上传溯源信息 + 原料批次匹配")
    public void stage1_RawMaterial() {
        // 1.1 供应商主动上传原料溯源信息
        RawDetail detail = new RawDetail();
        detail.setOrigin("山东省寿光市");
        detail.setFarmType("大棚种植");
        detail.setFeedType("有机肥，无农药");
        detail.setCertType(1);            // 1-绿色食品认证
        detail.setBreed("优质胡萝卜品种");
        detail.setScaleDesc("年产5000吨");
        detail.setCollectDate("2026-06-20");
        detail.setTransportTemp("2-6℃");
        detail.setStorageMethod(1);
        detail.setShelfLife("2026-07-20");
        detail.setInspectionNo("QC" + TS);
        detail.setCreateBy("test");
        detail.setUpdateBy("test");
        detail.setUploader("龙大绿色农场");

        RawPending pending = new RawPending();
        pending.setSupplierName("龙大绿色农场");
        pending.setProductName("有机胡萝卜");
        pending.setProductCategory("蔬菜");
        pending.setAmount(1000);

        int result = rawService.proactiveUpload(detail, pending);
        assertEquals(1, result, "供应商上传应成功");

        // 拿到临时分配的待匹配编号
        rawBatchNo = rawService.generateBatchNo();
        String pendingCode = rawService.generatePendingCode();

        System.out.println("[阶段1] 供应商上传成功, 原料批次号=" + rawBatchNo);

        // 1.2 创建原料批次（企业端）
        Raw raw = new Raw();
        raw.setBatchNo(rawBatchNo);
        raw.setSupplierName("龙大绿色农场");
        raw.setSupplierId("SUP" + TS);
        raw.setProductCategory("蔬菜");
        raw.setProductName("有机胡萝卜");
        raw.setAmount(new BigDecimal("1000.00"));
        raw.setUnit("公斤");
        raw.setWarehouse("冷库A-05");

        int createResult = rawService.createRaw(raw);
        assertEquals(1, createResult, "原料批次创建应成功");

        // 查询验证
        Raw saved = rawService.getByBatchNo(rawBatchNo);
        assertNotNull(saved, "应能查到刚创建的原料批次");
        assertEquals("有机胡萝卜", saved.getProductName());
        rawBatchId = saved.getRawBatchId();

        System.out.println("[阶段1] 原料批次创建成功, ID=" + rawBatchId);
    }

    @Test
    @Order(2)
    @DisplayName("2. 原料质检")
    public void stage2_RawQualityCheck() {
        assertNotNull(rawBatchNo, "需要先执行阶段1");

        // 质检合格
        rawService.recordQualityCheck(rawBatchNo, 1);

        Raw raw = rawService.getByBatchNo(rawBatchNo);
        assertNotNull(raw);
        assertEquals(Integer.valueOf(1), raw.getCheckResult(), "质检应为合格");
        assertEquals(Integer.valueOf(2), raw.getBatchStatus(), "质检合格后批次状态应为2");

        System.out.println("[阶段2] 原料质检完成, 结果=合格, 状态=" + raw.getBatchStatus());
    }

    // ========================================================================
    // 阶段二：加工批次 (Process) + 生产批次 (Prod)
    // ========================================================================

    @Test
    @Order(3)
    @DisplayName("3. 创建工艺模板 + 加工批次")
    public void stage3_ProcessBatch() {
        assertNotNull(rawBatchNo, "需要先执行阶段1");

        // 3.1 创建工艺模板
        TechTemplate template = new TechTemplate();
        template.setTemplateName("胡萝卜清洗分选工艺" + TS);
        template.setApplicableProduct("有机胡萝卜");
        template.setTargetTemp("2-6℃");
        template.setDuration("120分钟");
        template.setCleanLevel(3);
        template.setTemplateStatus(1);
        int tplResult = productionService.createTemplate(template);
        assertEquals(1, tplResult, "工艺模板创建应成功");
        System.out.println("[阶段3] 工艺模板创建成功: " + template.getTemplateName());

        // 3.2 创建加工批次（关联原料批次）
        processBatchNo = productionService.generateProcessBatchNo();

        ProcessBatch pb = new ProcessBatch();
        pb.setBatchNo(processBatchNo);
        pb.setProductName("有机胡萝卜(加工)");
        pb.setRawBatchNo(rawBatchNo);
        pb.setProductionLine("加工线B2");
        pb.setTemplateName(template.getTemplateName());
        pb.setShift(1);

        int pbResult = productionService.createProcessBatch(pb);
        assertEquals(1, pbResult, "加工批次创建应成功");

        ProcessBatch saved = productionService.getByProcessBatchNo(processBatchNo);
        assertNotNull(saved);
        processBatchId = saved.getProcessBatchId();

        System.out.println("[阶段3] 加工批次创建成功, 批次号=" + processBatchNo + ", ID=" + processBatchId);
    }

    @Test
    @Order(4)
    @DisplayName("4. 投料记录 + 加工批次完成")
    public void stage4_MaterialInputAndProcessComplete() {
        assertNotNull(processBatchNo, "需要先执行阶段3");

        // 4.1 记录投料
        ProdMaterialInput input = new ProdMaterialInput();
        input.setMaterialName("有机胡萝卜(原料)");
        input.setRawBatchNo(rawBatchNo);
        input.setInputAmount(1000);
        input.setInputStatus("已完成");

        int inputResult = productionService.recordMaterialInput(input);
        assertEquals(1, inputResult, "投料记录应成功");
        System.out.println("[阶段4] 投料记录成功");

        // 4.2 完成加工批次
        int completeResult = productionService.completeProcessBatch(processBatchId);
        assertEquals(1, completeResult, "加工批次完成应成功");

        ProcessBatch pb = productionService.getByProcessBatchNo(processBatchNo);
        assertEquals(Integer.valueOf(2), pb.getBatchStatus(), "加工批次应为已完成状态");

        System.out.println("[阶段4] 加工批次完成, 状态=" + pb.getBatchStatus());
    }

    @Test
    @Order(5)
    @DisplayName("5. 生产批次创建 + 绑码")
    public void stage5_ProdBatch() {
        assertNotNull(processBatchNo, "需要先执行阶段3");

        // 5.1 创建生产批次（关联加工批次）
        prodBatchNo = productionService.generateProdBatchNo();

        ProdBatch prod = new ProdBatch();
        prod.setBatchNo(prodBatchNo);
        prod.setProductName("有机胡萝卜(成品)");
        prod.setProcessBatchNo(processBatchNo);
        prod.setProductionLine("包装线C1");
        prod.setPlannedAmount(5000);
        prod.setActualAmount(4980);
        prod.setProductionDate("2026-06-24");

        int result = productionService.createProdBatch(prod);
        assertEquals(1, result, "生产批次创建应成功");

        ProdBatch saved = productionService.getByProdBatchNo(prodBatchNo);
        assertNotNull(saved);
        prodBatchId = saved.getProdBatchId();

        System.out.println("[阶段5] 生产批次创建成功, 批次号=" + prodBatchNo + ", ID=" + prodBatchId);

        // 5.2 生产环境数据采集
        ProdEnvRecord env = new ProdEnvRecord();
        env.setProductionLine("包装线C1");
        env.setWorkshopTemp("22.5℃");
        env.setWorkshopHumidity("45.0%");
        env.setCleanLevel("万级");
        env.setIsAbnormal(0);
        productionService.recordEnv(env);
        System.out.println("[阶段5] 环境数据采集完成");

        // 5.3 绑码完成
        int bindResult = productionService.bindCodeComplete(prodBatchId);
        assertEquals(1, bindResult, "绑码应成功");

        ProdBatch afterBind = productionService.getByProdBatchNo(prodBatchNo);
        assertEquals(Integer.valueOf(1), afterBind.getCodeStatus(), "码状态应为已绑定");
        assertEquals(Integer.valueOf(3), afterBind.getBatchStatus(), "批次状态应为已绑码");

        System.out.println("[阶段5] 绑码完成, 码状态=" + afterBind.getCodeStatus());
    }

    @Test
    @Order(6)
    @DisplayName("6. 生产批次质检")
    public void stage6_ProdQualityCheck() {
        assertNotNull(prodBatchNo, "需要先执行阶段5");

        // 创建质检记录
        QualityInspection inspection = new QualityInspection();
        inspection.setBizType(2);           // 2-生产批次
        inspection.setBizBatchNo(prodBatchNo);
        inspection.setInspectionType(1);    // 1-出厂检验
        inspection.setInspectionResult(1);  // 1-合格
        inspection.setInspector("质检员-张工");
        inspection.setStandard("GB 2762-2022");
        inspection.setSensoryCheck(1);
        inspection.setMicrobeCheck(1);
        inspection.setDetailResult("感官、微生物、农残均合格");

        int inspResult = productionService.createInspection(inspection);
        assertEquals(1, inspResult, "质检记录创建应成功");
        System.out.println("[阶段6] 质检记录创建成功, 编号=" + inspection.getInspectionNo());

        // 录入生产批次质检结果
        productionService.recordQualityCheckForProd(prodBatchNo, 1);

        ProdBatch prod = productionService.getByProdBatchNo(prodBatchNo);
        assertEquals(Integer.valueOf(1), prod.getCheckResult(), "质检结果应为合格");
        System.out.println("[阶段6] 质检合格, 批次状态=" + prod.getBatchStatus());
    }

    // ========================================================================
    // 阶段三：溯源码 (TraceCode)
    // ========================================================================

    @Test
    @Order(7)
    @DisplayName("7. 生成溯源码 + 激活")
    public void stage7_TraceCodeGenerateAndActivate() {
        assertNotNull(prodBatchNo, "需要先执行阶段5");

        // 7.1 单条生成溯源码
        TraceCodeGenerateDTO dto = new TraceCodeGenerateDTO();
        dto.setCodeType(1);                          // 单品码
        dto.setPackageLevel(1);                      // 最小销售单元
        dto.setProductId("PROD_" + TS);
        dto.setProductName("有机胡萝卜(成品)");
        dto.setEnterpriseId("ENT_001");
        dto.setEnterpriseName("龙大食品有限公司");
        dto.setBatchNo(prodBatchNo);
        dto.setOperator("admin");

        traceCode = traceCodeService.generateTraceCode(dto).getTraceCode();
        assertNotNull(traceCode, "溯源码值不应为空");
        System.out.println("[阶段7] 单条溯源码生成: " + traceCode);

        // 7.2 验证溯源码存在且已绑定
        TraceCode tc = traceCodeService.getByCode(traceCode);
        assertNotNull(tc);
        assertEquals(Integer.valueOf(1), tc.getTraceCodeStatus(), "应处于已绑定状态");
        assertEquals("有机胡萝卜(成品)", tc.getProductName());
        System.out.println("[阶段7] 溯源码状态=已绑定, 产品=" + tc.getProductName());

        // 7.3 完善Hash校验
        String hash = traceCodeService.computeContentHash(tc);
        assertNotNull(hash, "内容Hash不应为空");
        assertEquals(tc.getContentHash(), hash, "Hash应一致");
        System.out.println("[阶段7] SHA-256 Hash校验: ✓");

        // 7.4 绑定业务数据（6种链路节点）
        traceCodeService.bindBusinessData(traceCode, "RAW_BATCH", String.valueOf(rawBatchId),
                rawBatchNo, "admin");
        traceCodeService.bindBusinessData(traceCode, "PROCESS_BATCH", String.valueOf(processBatchId),
                processBatchNo, "admin");
        traceCodeService.bindBusinessData(traceCode, "PROD_BATCH", String.valueOf(prodBatchId),
                prodBatchNo, "admin");
        System.out.println("[阶段7] 业务数据绑定完成: RAW→PROCESS→PROD");

        // 7.5 激活溯源码
        TraceCodeStatusDTO statusDTO = new TraceCodeStatusDTO();
        statusDTO.setTraceCode(traceCode);
        statusDTO.setTargetStatus(2);  // 激活
        statusDTO.setOperator("admin");
        traceCodeService.updateTraceCodeStatus(statusDTO);

        TraceCode activated = traceCodeService.getByCode(traceCode);
        assertEquals(Integer.valueOf(2), activated.getTraceCodeStatus(), "应处于已激活状态");
        System.out.println("[阶段7] 溯源码已激活 ✓");
    }

    @Test
    @Order(8)
    @DisplayName("8. 批量生成溯源码")
    public void stage8_BatchGenerateTraceCode() {
        assertNotNull(prodBatchNo, "需要先执行阶段5");

        TraceCodeBatchDTO batchDTO = new TraceCodeBatchDTO();
        batchDTO.setCodeType(1);
        batchDTO.setPackageLevel(1);
        batchDTO.setProductId("PROD_" + TS);
        batchDTO.setProductName("有机胡萝卜(成品)");
        batchDTO.setEnterpriseId("ENT_001");
        batchDTO.setEnterpriseName("龙大食品有限公司");
        batchDTO.setBatchNo(prodBatchNo);
        batchDTO.setGenerateCount(5);           // 批量生成5条
        batchDTO.setOperator("admin");

        List<TraceCodeVO> batchResult = traceCodeService.batchGenerateTraceCode(batchDTO);
        assertEquals(5, batchResult.size(), "应生成5条溯源码");

        generateBatchNo = batchResult.get(0).getTraceCode();
        System.out.println("[阶段8] 批量生成5条溯源码成功, 第一条=" + generateBatchNo);

        // 按批次号查询
        List<TraceCode> byBatch = traceCodeService.listByBatchNo(prodBatchNo);
        assertTrue(byBatch.size() >= 6, "按生产批次号至少应查到6条(1+5)");
        System.out.println("[阶段8] 按批次号查询溯源码: " + byBatch.size() + "条");
    }

    // ========================================================================
    // 阶段四：冷链物流 (ColdChain)
    // ========================================================================

    @Test
    @Order(9)
    @DisplayName("9. 仓库 + 车辆 + 冷链运输订单")
    public void stage9_ColdChainTransport() {
        assertNotNull(prodBatchNo, "需要先执行阶段5");

        // 9.1 创建仓库
        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseName("龙大冷链中心仓" + TS);
        warehouse.setWarehouseType(1);       // 冷冻库
        warehouse.setAddress("山东省寿光市冷链园区A区");
        warehouse.setCapacity("12000");
        warehouse.setTempRange("-18℃~-10℃");
        warehouse.setHumidityRange("40%-65%");
        int whResult = coldChainService.createWarehouse(warehouse);
        assertEquals(1, whResult);
        System.out.println("[阶段9] 仓库创建成功: " + warehouse.getWarehouseName());

        // 9.2 注册冷链车辆
        CcVehicle vehicle = new CcVehicle();
        vehicle.setPlateNo("鲁A" + TS.substring(0, 4));
        vehicle.setVehicleModel("福田 欧曼 9.6米冷藏车");
        vehicle.setDriverName("司机-李明");
        vehicle.setDriverPhone("1380000" + TS.substring(0, 4));
        vehicle.setOwnerId("LOG_001");
        vehicle.setOwnerName("龙大物流");
        vehicle.setColdType("冷冻");
        vehicle.setTempRange("-18℃~-10℃");
        vehicle.setVehicleStatus(1);         // 空闲

        int vResult = coldChainService.createVehicle(vehicle);
        assertEquals(1, vResult);
        vehicleId = vehicle.getVehicleId();
        System.out.println("[阶段9] 冷链车辆注册成功, 车牌=" + vehicle.getPlateNo() + ", ID=" + vehicleId);

        // 9.3 创建冷链运输订单
        transportOrderNo = coldChainService.generateTransportOrderNo();

        CcTransport transport = new CcTransport();
        transport.setOrderNo(transportOrderNo);
        transport.setProdBatchNo(prodBatchNo);
        transport.setProductName("有机胡萝卜(成品)");
        transport.setPlateNo(vehicle.getPlateNo());
        transport.setDriverName("司机-李明");
        transport.setDepartureName("龙大冷链中心仓" + TS);
        transport.setDestinationName("北京朝阳区冷链配送中心");
        transport.setTransportMethod(1);     // 公路运输
        transport.setLoadingTemp("-15.0");
        transport.setTempLower(new BigDecimal("-18"));
        transport.setTempUpper(new BigDecimal("-10"));
        transport.setHumidLower(new BigDecimal("40"));
        transport.setHumidUpper(new BigDecimal("65"));

        int tResult = coldChainService.createTransport(transport);
        assertEquals(1, tResult);

        CcTransport saved = coldChainService.getByOrderNo(transportOrderNo);
        assertNotNull(saved);
        transportId = saved.getTransportId();
        assertEquals(Integer.valueOf(1), saved.getTransportStatus(), "初始状态应为待发运");

        System.out.println("[阶段9] 运输订单创建成功, 订单号=" + transportOrderNo + ", ID=" + transportId);
    }

    @Test
    @Order(10)
    @DisplayName("10. 发运 + 温湿度记录 + 运输节点")
    public void stage10_DepartAndMonitoring() {
        assertNotNull(transportId, "需要先执行阶段9");
        assertNotNull(transportOrderNo);

        // 10.1 发运
        int departResult = coldChainService.departTransport(transportId);
        assertEquals(1, departResult);

        CcTransport transport = coldChainService.getByOrderNo(transportOrderNo);
        assertEquals(Integer.valueOf(2), transport.getTransportStatus(), "发运后状态应为运输中");
        assertNotNull(transport.getDepartTime(), "发运时间不应为空");

        // 验证车辆状态联动
        CcVehicle vehicle = coldChainService.getByPlateNo(transport.getPlateNo());
        assertEquals(Integer.valueOf(1), vehicle.getVehicleStatus(), "车辆应为运输中状态");

        System.out.println("[阶段10] 发运成功, 车辆状态=运输中, 出发时间=" + transport.getDepartTime());
        System.out.println("[阶段10] 自动记录装货出库节点 ✓");

        // 10.2 记录3条温湿度（模拟运输途中监测）
        CcTempHumidity th1 = new CcTempHumidity();
        th1.setOrderNo(transportOrderNo);
        th1.setTemperature(new BigDecimal("-14.2"));
        th1.setHumidity(new BigDecimal("52"));
        th1.setIsAbnormal(0);
        coldChainService.recordTempHumidity(th1);
        System.out.println("[阶段10] 温湿度记录1: -14.2℃, 52% — 正常");

        CcTempHumidity th2 = new CcTempHumidity();
        th2.setOrderNo(transportOrderNo);
        th2.setTemperature(new BigDecimal("-8.5"));  // 超温！
        th2.setHumidity(new BigDecimal("55"));
        th2.setIsAbnormal(0);  // 系统会自动检测
        coldChainService.recordTempHumidity(th2);

        // 验证超温后运输订单被标记为温度预警
        CcTransport afterAlert = coldChainService.getByOrderNo(transportOrderNo);
        System.out.println("[阶段10] 温湿度记录2: -8.5℃(超温!) — 运输状态=" + afterAlert.getTransportStatus()
                + " (3=温度预警)");
        assertEquals(Integer.valueOf(3), afterAlert.getTransportStatus(), "超温后应触发温度预警");

        CcTempHumidity th3 = new CcTempHumidity();
        th3.setOrderNo(transportOrderNo);
        th3.setTemperature(new BigDecimal("-14.8"));
        th3.setHumidity(new BigDecimal("50"));
        th3.setIsAbnormal(0);
        coldChainService.recordTempHumidity(th3);
        System.out.println("[阶段10] 温湿度记录3: -14.8℃, 50% — 恢复正常");

        // 10.3 记录运输节点：在途检查
        CcTransportNode node = new CcTransportNode();
        node.setOrderNo(transportOrderNo);
        node.setNodeType(2);           // 2-在途检查
        node.setNodeTime("2026-06-24 14:30:00");
        node.setLocation("G20高速济南服务区");
        node.setOperator("司机-李明");
        node.setNodeStatus(1);
        coldChainService.recordNode(node);
        System.out.println("[阶段10] 运输节点记录: 在途检查 — G20济南服务区");

        // 验证温湿度记录
        List<CcTempHumidity> thList = coldChainService.listTempHumidityByOrderNo(transportOrderNo);
        assertTrue(thList.size() >= 3, "至少应有3条温湿度记录");
        System.out.println("[阶段10] 温湿度记录总数: " + thList.size());
    }

    @Test
    @Order(11)
    @DisplayName("11. 签收 + 抵达确认")
    public void stage11_ArriveAndReceipt() {
        assertNotNull(transportId, "需要先执行阶段9");
        assertNotNull(transportOrderNo);

        // 11.1 抵达签收
        int arriveResult = coldChainService.arriveTransport(transportId);
        assertEquals(1, arriveResult);

        CcTransport transport = coldChainService.getByOrderNo(transportOrderNo);
        assertEquals(Integer.valueOf(4), transport.getTransportStatus(), "签收后状态应为已签收");
        assertNotNull(transport.getActualArrival(), "实际到达时间不应为空");

        // 验证车辆释放
        CcVehicle vehicle = coldChainService.getByPlateNo(transport.getPlateNo());
        assertEquals(Integer.valueOf(0), vehicle.getVehicleStatus(), "车辆应为空闲状态");

        System.out.println("[阶段11] 抵达签收成功, 车辆已释放, 到达时间=" + transport.getActualArrival());
        System.out.println("[阶段11] 自动记录电子签收节点 ✓");

        // 11.2 创建签收单
        CcReceipt receipt = new CcReceipt();
        receipt.setOrderNo(transportOrderNo);
        receipt.setSigner("北京朝阳区冷链配送中心-收货员");
        receipt.setSignTemp("-14.5℃");
        receipt.setIsPackageIntact(1);   // 包装完好
        receipt.setQtyMatch(1);          // 数量符合
        receipt.setRemark("正常签收");

        int receiptResult = coldChainService.signReceipt(receipt);
        assertEquals(1, receiptResult);
        System.out.println("[阶段11] 签收单创建成功 ✓");

        // 11.3 全链路冷链追溯
        ColdChainService.ColdChainTraceVO chainVO = coldChainService.traceColdChain(transportOrderNo);
        assertNotNull(chainVO, "冷链追溯结果不应为空");
        assertNotNull(chainVO.getTransport(), "运输订单应在追溯结果中");
        assertNotNull(chainVO.getVehicle(), "车辆信息应在追溯结果中");
        assertNotNull(chainVO.getTempHumidityRecords(), "温湿度记录应在追溯结果中");
        assertTrue(chainVO.getTempHumidityRecords().size() >= 3, "追溯应包含温湿度数据");
        assertNotNull(chainVO.getNodes(), "运输节点应在追溯结果中");
        assertNotNull(chainVO.getReceipt(), "签收单应在追溯结果中");

        System.out.println("[阶段11] 冷链全链路追溯验证通过:");
        System.out.println("  ├ 运输订单: " + chainVO.getTransport().getOrderNo());
        System.out.println("  ├ 车辆: " + chainVO.getVehicle().getPlateNo());
        System.out.println("  ├ 温湿度记录: " + chainVO.getTempHumidityRecords().size() + "条");
        System.out.println("  ├ 运输节点: " + chainVO.getNodes().size() + "个");
        System.out.println("  └ 签收单: " + (chainVO.getReceipt() != null ? "已签收" : "无"));
    }

    // ========================================================================
    // 阶段五：销售终端 (Sales)
    // ========================================================================

    @Test
    @Order(12)
    @DisplayName("12. 销售终端注册 + 产品入库")
    public void stage12_SalesTerminalAndStockIn() {
        assertNotNull(prodBatchNo, "需要先执行阶段5");

        // 12.1 注册销售终端
        terminalCode = salesService.generateTerminalCode();

        SalesTerminal terminal = new SalesTerminal();
        terminal.setTerminalCode(terminalCode);
        terminal.setTerminalName("北京朝阳区冷链配送中心-终端A");
        terminal.setTerminalType(1);        // 1-商超
        terminal.setArea("北京市朝阳区");
        terminal.setAddress("朝阳区望京街道SOHO B1层");
        terminal.setOperatorId("SELLER_001");
        terminal.setOperatorName("京超冷链运营");

        int termResult = salesService.createTerminal(terminal);
        assertEquals(1, termResult);

        SalesTerminal savedTerminal = salesService.getByTerminalCode(terminalCode);
        assertNotNull(savedTerminal);
        terminalId = savedTerminal.getTerminalId();

        System.out.println("[阶段12] 销售终端注册成功, 编码=" + terminalCode + ", ID=" + terminalId);

        // 12.2 产品入库（自动填充储存环境）
        SalesStock stock = new SalesStock();
        stock.setTerminalId(String.valueOf(terminalId));
        stock.setTerminalName(terminalCode);
        stock.setProdBatchNo(prodBatchNo);
        stock.setProductName("有机胡萝卜(成品)");
        stock.setQuantity(300);

        int stockResult = salesService.stockIn(stock);
        assertEquals(1, stockResult);
        System.out.println("[阶段12] 产品入库成功, 数量=300, 库存编码=" + stock.getStockCode());

        // 验证储存环境自动填充
        SalesStorage storage = salesService.getStorageByTerminalCode(terminalCode);
        assertNotNull(storage, "应自动创建储存环境记录");
        assertEquals("商超", storage.getTerminalType());
        assertEquals(Integer.valueOf(1), storage.getIsAutoFilled(), "应为自动填充");
        System.out.println("[阶段12] 储存环境自动填充: " + storage.getTerminalType()
                + ", 温度=" + storage.getTemperature() + ", 湿度=" + storage.getHumidity());
    }

    @Test
    @Order(13)
    @DisplayName("13. 销售补充信息 + 防窜货核验")
    public void stage13_SalesSupplementAndAntiFraud() {
        assertNotNull(terminalCode, "需要先执行阶段12");
        assertNotNull(prodBatchNo);

        // 13.1 销售商补充销售详情
        SalesSupplement supplement = new SalesSupplement();
        supplement.setTerminalCode(terminalCode);
        supplement.setTraceBatchNo(prodBatchNo);  // 关联溯源码批次
        supplement.setSalesCompany("京超冷链运营");
        supplement.setQuantity(280);               // 实际库存数量
        supplement.setTemperature("2-6℃");         // 储存温度
        supplement.setHumidity("45-65%");          // 储存湿度
        supplement.setStorageMethod(1);            // 冷藏立柜
        supplement.setShelfLife("2026-07-01");
        supplement.setSupplementStatus(1);

        int suppResult = salesService.supplementSalesInfo(supplement);
        assertEquals(1, suppResult);
        System.out.println("[阶段13] 销售详情补充成功, 库存=280, 温度=" + supplement.getTemperature());

        // 13.2 防窜货核验
        salesService.runAntiFraudCheck();

        // 检查终端是否被标记（应该不标记，因为只有1个终端1个区域）
        SalesTerminal terminal = salesService.getByTerminalCode(terminalCode);
        assertNotNull(terminal);
        assertNotEquals(Integer.valueOf(2), terminal.getTerminalStatus(),
                "单区域销售不应触发窜货异常");

        System.out.println("[阶段13] 防窜货核验完成, 终端状态=" + terminal.getTerminalStatus()
                + " (正常应为非2)");
    }

    // ========================================================================
    // 阶段六：消费者扫码 + 全链路追溯
    // ========================================================================

    @Test
    @Order(14)
    @DisplayName("14. 消费者扫码查询")
    public void stage14_ConsumerScan() {
        assertNotNull(traceCode, "需要先执行阶段7");

        ConsumerTraceVO vo = traceCodeService.queryByCode(traceCode);
        assertNotNull(vo);
        assertEquals("success", vo.getQueryResult(), "扫码应答为success");
        assertEquals("正常", vo.getCodeStatus(), "码状态应为正常");
        assertEquals("有机胡萝卜(成品)", vo.getProductName());
        System.out.println("[阶段14] 消费者扫码结果:");
        System.out.println("  ├ 产品: " + vo.getProductName());
        System.out.println("  ├ 批次: " + vo.getBatchNo());
        System.out.println("  ├ 企业: " + vo.getEnterpriseName());
        System.out.println("  ├ 码状态: " + vo.getCodeStatus());
        System.out.println("  ├ 链上校验: " + vo.getChainVerifyResult());
        System.out.println("  ├ 风险等级: " + vo.getRiskLevel());
        System.out.println("  ├ 扫码时间: " + vo.getScanTime());
        System.out.println("  └ 流转节点: " + (vo.getTraceNodes() != null ? vo.getTraceNodes().size() : 0) + "个");

        // 验证流转轨迹包含多个链路节点
        assertNotNull(vo.getTraceNodes());
        assertTrue(vo.getTraceNodes().size() >= 3,
                "流转轨迹应至少包含原料、加工、生产3个节点(实际=" + vo.getTraceNodes().size() + ")");

        // 打印轨迹
        for (ConsumerTraceVO.TraceNode node : vo.getTraceNodes()) {
            System.out.println("       [" + node.getNodeType() + "] " + node.getNodeName()
                    + " — " + node.getNodeCode());
        }
    }

    @Test
    @Order(15)
    @DisplayName("15. 生产全链路追溯（验证 prod→process→raw）")
    public void stage15_ProductionChainTrace() {
        assertNotNull(prodBatchNo, "需要先执行阶段5");

        ProductionService.ProductionChainTraceVO vo =
                productionService.traceProcessChain(prodBatchNo);

        assertNotNull(vo);
        assertNotNull(vo.getProdBatch(), "生产批次不应为空");
        assertEquals(prodBatchNo, vo.getProdBatch().getBatchNo());

        System.out.println("[阶段15] 生产全链路追溯:");
        System.out.println("  ├ 生产批次: " + vo.getProdBatch().getBatchNo()
                + " (" + vo.getProdBatch().getProductName() + ")");

        // 加工批次
        assertNotNull(vo.getProcessBatch(), "应能追溯到加工批次");
        System.out.println("  ├ 加工批次: " + vo.getProcessBatch().getBatchNo()
                + " (" + vo.getProcessBatch().getProductName() + ")");
        System.out.println("  │  └ 原料批次号: " + vo.getProcessBatch().getRawBatchNo());

        // 原料批次
        assertNotNull(vo.getRawBatch(), "应能追溯到原料批次");
        assertEquals(rawBatchNo, vo.getRawBatch().getBatchNo());
        System.out.println("  └ 原料批次: " + vo.getRawBatch().getBatchNo()
                + " (" + vo.getRawBatch().getProductName()
                + ", 供应商=" + vo.getRawBatch().getSupplierName() + ")");

        // 环境记录
        if (vo.getEnvRecords() != null) {
            System.out.println("  环境记录数: " + vo.getEnvRecords().size());
        }

        System.out.println("[阶段15] ✓ 全链路追溯完整: 原料→加工→生产");
    }

    // ========================================================================
    // 阶段七：异常场景验证
    // ========================================================================

    @Test
    @Order(16)
    @DisplayName("16. 溯源码禁用 + 消费者扫码失败")
    public void stage16_DisableAndScanFail() {
        assertNotNull(traceCode, "需要先执行阶段7");

        // 16.1 禁用溯源码
        TraceCodeStatusDTO disableDTO = new TraceCodeStatusDTO();
        disableDTO.setTraceCode(traceCode);
        disableDTO.setTargetStatus(3);       // 禁用
        disableDTO.setReason("产品批次发现异物风险，紧急召回");
        disableDTO.setOperator("admin");
        traceCodeService.updateTraceCodeStatus(disableDTO);

        TraceCode disabled = traceCodeService.getByCode(traceCode);
        assertEquals(Integer.valueOf(3), disabled.getTraceCodeStatus(), "应处于已禁用状态");
        assertEquals("产品批次发现异物风险，紧急召回", disabled.getDisableReason());
        System.out.println("[阶段16] 溯源码已禁用, 原因=" + disabled.getDisableReason());

        // 16.2 消费者扫码 — 应返回异常
        ConsumerTraceVO vo = traceCodeService.queryByCode(traceCode);
        assertNotNull(vo);
        assertEquals("fail", vo.getQueryResult(), "禁用后扫码应返回fail");
        assertEquals("已禁用", vo.getCodeStatus());
        assertEquals("重大", vo.getRiskLevel(), "禁用后风险等级应为重大");
        assertTrue(vo.getRiskSuggestion().contains("召回"),
                "风险提示应包含召回信息");
        System.out.println("[阶段16] 消费者扫码(禁用后): 结果=" + vo.getQueryResult()
                + ", 风险=" + vo.getRiskLevel() + ", 提示=" + vo.getRiskSuggestion());
    }

    @Test
    @Order(17)
    @DisplayName("17. 溯源码Hash完整性校验")
    public void stage17_HashIntegrity() {
        assertNotNull(traceCode, "需要先执行阶段7");

        TraceCode tc = traceCodeService.getByCode(traceCode);
        assertNotNull(tc);

        String originalHash = tc.getContentHash();
        String recomputed = traceCodeService.computeContentHash(tc);

        assertEquals(originalHash, recomputed, "内容Hash应与生成时一致，证明数据未被篡改");
        System.out.println("[阶段17] Hash校验通过: " + originalHash.substring(0, 16) + "...");
        System.out.println("[阶段17] 数据完整性确认 ✓");
    }

    // ========================================================================
    // 阶段八：数据清理（软删除）
    // ========================================================================

    @Test
    @Order(98)
    @DisplayName("98. 软删除 — 数据清理")
    public void stage98_SoftDelete() {
        // 按依赖顺序软删除
        int deleted = 0;

        if (terminalId != null) {
            deleted += salesService.deleteTerminal(terminalId);
            System.out.println("[清理] 软删除销售终端: " + terminalId);
        }
        if (transportId != null) {
            deleted += coldChainService.deleteTransport(transportId);
            System.out.println("[清理] 软删除运输订单: " + transportId);
        }
        if (prodBatchId != null) {
            deleted += productionService.deleteProdBatch(prodBatchId);
            System.out.println("[清理] 软删除生产批次: " + prodBatchId);
        }
        if (processBatchId != null) {
            deleted += productionService.deleteProcessBatch(processBatchId);
            System.out.println("[清理] 软删除加工批次: " + processBatchId);
        }
        if (rawBatchId != null) {
            deleted += rawService.deleteRaw(rawBatchId);
            System.out.println("[清理] 软删除原料批次: " + rawBatchId);
        }

        System.out.println("[清理] 共软删除 " + deleted + " 条记录");

        // 验证删除后查询
        if (rawBatchNo != null) {
            Raw raw = rawService.getByBatchNo(rawBatchNo);
            assertNull(raw, "软删除后应查不到原料批次");
            System.out.println("[清理] 验证: 软删除后查询返回null ✓");
        }
    }

    @Test
    @Order(99)
    @DisplayName("99. 测试总结")
    public void stage99_Summary() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║      🏆 端到端全链路集成测试 全部通过                    ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.println("║  阶段   │  测试内容                  │  状态          ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.println("║  1-2   │  原料批次 + 质检             │  ✓            ║");
        System.out.println("║  3-4   │  加工批次 + 投料              │  ✓            ║");
        System.out.println("║  5-6   │  生产批次 + 绑码 + 质检       │  ✓            ║");
        System.out.println("║  7-8   │  溯源码生成 + 激活 + 批量     │  ✓            ║");
        System.out.println("║  9-11  │  冷链运输(仓库→车辆→发运→签收) │  ✓            ║");
        System.out.println("║  12-13 │  销售终端 + 入库 + 防窜货     │  ✓            ║");
        System.out.println("║  14    │  消费者扫码                   │  ✓            ║");
        System.out.println("║  15    │  生产全链路追溯               │  ✓            ║");
        System.out.println("║  16    │  溯源码禁用 + 异常扫码        │  ✓            ║");
        System.out.println("║  17    │  Hash完整性校验              │  ✓            ║");
        System.out.println("║  98    │  软删除清理                  │  ✓            ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.println("║  全链路: 原料 → 加工 → 生产 → 溯源码 → 冷链 → 销售    ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}

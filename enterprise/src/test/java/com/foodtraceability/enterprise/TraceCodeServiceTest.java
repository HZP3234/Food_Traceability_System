package com.foodtraceability.enterprise;

import com.foodtraceability.enterprise.dto.*;
import com.foodtraceability.enterprise.entity.TraceCode;
import com.foodtraceability.enterprise.entity.TraceCodeBind;
import com.foodtraceability.enterprise.service.TraceCodeService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 溯源码管理模块 — 单元测试
 * <p>
 * 测试覆盖：
 * 1. 单条溯源码生成
 * 2. 批量生成溯源码
 * 3. 溯源码详情查询（含绑定关系）
 * 4. 溯源码状态变更（启用/禁用/作废）
 * 5. 消费者扫码查询（公开接口）
 * 6. 溯源码校验（内部接口）
 * 7. 业务数据绑定
 * 8. 分页查询
 * 9. Hash 完整性校验
 * 10. 边界条件与异常场景
 *
 * @author GuangYao Liu
 * @since 2026-06-23
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TraceCodeServiceTest {

    @Autowired
    private TraceCodeService traceCodeService;

    /** 记录测试生成的溯源码，便于后续测试复用和清理 */
    private static String testTraceCode;
    private static String testBatchGenerateNo;

    // ==================== 1. 单条溯源码生成测试 ====================

    @Test
    @Order(1)
    @DisplayName("单条溯源码生成 — 正常流程")
    public void testGenerateTraceCode() {
        // ---------- 准备请求数据 ----------
        TraceCodeGenerateDTO dto = new TraceCodeGenerateDTO();
        dto.setCodeType(1);                      // 单品码
        dto.setPackageLevel(1);                  // 最小销售单元
        dto.setProductId("PROD2026001");
        dto.setProductName("伊利纯牛奶250ml");
        dto.setEnterpriseId("ENT2026001");
        dto.setEnterpriseName("内蒙古伊利实业集团股份有限公司");
        dto.setBatchNo("PBS20260623001");
        dto.setQualityResult(1);                 // 合格
        dto.setQualityReportUrl("http://example.com/report/PBS20260623001.pdf");
        dto.setExpireTime("2027-06-23 23:59:59");
        dto.setOperator("admin");

        // ---------- 调用生成 ----------
        TraceCodeVO vo = traceCodeService.generateTraceCode(dto);

        // ---------- 断言验证 ----------
        assertNotNull(vo, "返回结果不应为 null");
        assertNotNull(vo.getTraceCode(), "溯源码值不应为 null");
        assertTrue(vo.getTraceCode().startsWith("TC"), "单品码应以 TC 开头");
        assertEquals("单品码", vo.getCodeTypeName(), "码类型名称应为「单品码」");
        assertEquals("伊利纯牛奶250ml", vo.getProductName());
        assertEquals("内蒙古伊利实业集团股份有限公司", vo.getEnterpriseName());
        assertEquals("PBS20260623001", vo.getBatchNo());
        assertEquals("已绑定", vo.getTraceCodeStatusName(), "新生成的码状态应为「已绑定」");
        assertNotNull(vo.getContentHash(), "内容哈希不应为 null");
        assertEquals(Integer.valueOf(1), vo.getIsOnChain(), "应已上链");

        // 保存供后续测试使用
        testTraceCode = vo.getTraceCode();

        System.out.println("========== 单条溯源码生成测试通过 ==========");
        System.out.println("溯源码: " + vo.getTraceCode());
        System.out.println("码类型: " + vo.getCodeTypeName());
        System.out.println("产品: " + vo.getProductName());
        System.out.println("状态: " + vo.getTraceCodeStatusName());
        System.out.println("哈希: " + vo.getContentHash());
        System.out.println("已上链: " + (vo.getIsOnChain() == 1 ? "是" : "否"));
    }

    // ==================== 2. 批量生成溯源码测试 ====================

    @Test
    @Order(2)
    @DisplayName("批量生成溯源码 — 正常流程")
    public void testBatchGenerateTraceCode() {
        // ---------- 准备请求数据 ----------
        TraceCodeBatchDTO dto = new TraceCodeBatchDTO();
        dto.setCodeType(1);                      // 单品码
        dto.setPackageLevel(1);
        dto.setProductId("PROD2026002");
        dto.setProductName("蒙牛纯甄酸奶200g");
        dto.setEnterpriseId("ENT2026002");
        dto.setEnterpriseName("蒙牛乳业(集团)股份有限公司");
        dto.setBatchNo("PBS20260623002");
        dto.setGenerateCount(10);                // 批量生成10条
        dto.setQualityResult(1);
        dto.setExpireTime("2027-06-23 23:59:59");
        dto.setOperator("admin");

        // ---------- 调用批量生成 ----------
        List<TraceCodeVO> resultList = traceCodeService.batchGenerateTraceCode(dto);

        // ---------- 断言验证 ----------
        assertNotNull(resultList, "返回结果列表不应为 null");
        assertEquals(10, resultList.size(), "应生成10条溯源码");

        for (TraceCodeVO vo : resultList) {
            assertNotNull(vo.getTraceCode(), "每条溯源码值不应为 null");
            assertTrue(vo.getTraceCode().startsWith("TC"), "单品码应以 TC 开头");
            assertEquals("已绑定", vo.getTraceCodeStatusName());
            assertNotNull(vo.getContentHash());
        }

        // 记录批量生成批次号
        TraceCode firstCode = traceCodeService.getByCode(resultList.get(0).getTraceCode());
        testBatchGenerateNo = firstCode.getGenerateBatchNo();

        System.out.println("========== 批量生成溯源码测试通过 ==========");
        System.out.println("批量生成数量: " + resultList.size());
        System.out.println("生成批次号: " + testBatchGenerateNo);
        for (int i = 0; i < Math.min(3, resultList.size()); i++) {
            System.out.println("  [" + i + "] " + resultList.get(i).getTraceCode());
        }
        System.out.println("  ... 共 " + resultList.size() + " 条");
    }

    // ==================== 3. 批量生成 — 超出上限测试 ====================

    @Test
    @Order(3)
    @DisplayName("批量生成 — 超出上限应抛出异常")
    public void testBatchGenerateExceedLimit() {
        TraceCodeBatchDTO dto = new TraceCodeBatchDTO();
        dto.setCodeType(1);
        dto.setPackageLevel(1);
        dto.setProductId("PROD2026003");
        dto.setProductName("测试产品");
        dto.setEnterpriseId("ENT2026003");
        dto.setEnterpriseName("测试企业");
        dto.setBatchNo("PBS20260623003");
        dto.setGenerateCount(99999);             // 超出10000上限
        dto.setQualityResult(1);
        dto.setOperator("admin");

        assertThrows(RuntimeException.class, () -> {
            traceCodeService.batchGenerateTraceCode(dto);
        }, "超出上限应抛出 RuntimeException");

        System.out.println("========== 批量生成超限测试通过 ==========");
    }

    // ==================== 4. 溯源码详情查询测试 ====================

    @Test
    @Order(4)
    @DisplayName("查询溯源码详情 — 含绑定关系")
    public void testQueryTraceCodeDetail() {
        assertNotNull(testTraceCode, "需要先执行 testGenerateTraceCode");

        // ---------- 查询详情 ----------
        TraceCodeDetailVO detail = traceCodeService.queryTraceCodeDetail(testTraceCode);

        // ---------- 断言验证 ----------
        assertNotNull(detail, "详情不应为 null");
        assertEquals(testTraceCode, detail.getTraceCode());
        assertNotNull(detail.getBindList(), "绑定列表不应为 null");
        assertTrue(detail.getBindList().isEmpty(), "fts 当前未提供绑定关系表，绑定列表应为空");

        System.out.println("========== 溯源码详情查询测试通过 ==========");
        System.out.println("溯源码: " + detail.getTraceCode());
        System.out.println("绑定记录数: " + detail.getBindList().size());
    }

    // ==================== 5. 状态变更 — 启用测试 ====================

    @Test
    @Order(5)
    @DisplayName("溯源码状态变更 — 启用（激活）")
    public void testActivateTraceCode() {
        assertNotNull(testTraceCode, "需要先执行 testGenerateTraceCode");

        // ---------- 激活溯源码 ----------
        TraceCodeStatusDTO dto = new TraceCodeStatusDTO();
        dto.setTraceCode(testTraceCode);
        dto.setTargetStatus(2);  // 已激活
        dto.setOperator("admin");
        traceCodeService.updateTraceCodeStatus(dto);

        // ---------- 验证状态 ----------
        TraceCode tc = traceCodeService.getByCode(testTraceCode);
        assertNotNull(tc, "溯源码应存在");
        assertEquals(Integer.valueOf(2), tc.getTraceCodeStatus(), "状态应为「已激活」");
        assertNotNull(tc.getEnableTime(), "启用时间不应为 null");

        System.out.println("========== 溯源码启用测试通过 ==========");
        System.out.println("溯源码: " + testTraceCode + " 已激活");
        System.out.println("启用时间: " + tc.getEnableTime());
    }

    // ==================== 6. 消费者扫码查询测试 ====================

    @Test
    @Order(6)
    @DisplayName("消费者扫码查询 — 正常码")
    public void testConsumerScanSuccess() {
        assertNotNull(testTraceCode, "需要先执行 testGenerateTraceCode");

        // ---------- 扫码查询 ----------
        ConsumerTraceVO result = traceCodeService.queryByCode(testTraceCode);

        // ---------- 断言验证 ----------
        assertNotNull(result, "扫码结果不应为 null");
        assertEquals("success", result.getQueryResult(), "查询结果应为 success");
        assertNotNull(result.getProductName());
        assertNotNull(result.getBatchNo());
        assertNotNull(result.getTraceNodes(), "流转轨迹不应为 null");
        assertTrue(result.getTraceNodes().size() > 0, "至少有一个流转节点");

        System.out.println("========== 消费者扫码查询测试通过 ==========");
        System.out.println("产品: " + result.getProductName());
        System.out.println("批次: " + result.getBatchNo());
        System.out.println("码状态: " + result.getCodeStatus());
        System.out.println("质检: " + result.getQualityResult());
        System.out.println("链上校验: " + result.getChainVerifyResult());
        System.out.println("风险等级: " + result.getRiskLevel());
        System.out.println("节点数: " + result.getTraceNodes().size());
    }

    // ==================== 7. 消费者扫码 — 无效码测试 ====================

    @Test
    @Order(7)
    @DisplayName("消费者扫码查询 — 无效码")
    public void testConsumerScanInvalidCode() {
        ConsumerTraceVO result = traceCodeService.queryByCode("INVALID_CODE_99999");

        assertNotNull(result, "无效码也应返回结果对象");
        assertEquals("无效码", result.getCodeStatus());
        assertEquals("fail", result.getQueryResult());
        assertEquals("重大", result.getRiskLevel());

        System.out.println("========== 无效码扫码测试通过 ==========");
        System.out.println("状态: " + result.getCodeStatus());
        System.out.println("提示: " + result.getRiskSuggestion());
    }

    // ==================== 8. 状态变更 — 禁用测试 ====================

    @Test
    @Order(8)
    @DisplayName("溯源码状态变更 — 禁用")
    public void testDisableTraceCode() {
        assertNotNull(testTraceCode, "需要先执行 testGenerateTraceCode");

        // ---------- 禁用溯源码 ----------
        TraceCodeStatusDTO dto = new TraceCodeStatusDTO();
        dto.setTraceCode(testTraceCode);
        dto.setTargetStatus(3);  // 已禁用
        dto.setReason("产品疑似质量问题，临时禁用");
        dto.setOperator("quality_admin");
        traceCodeService.updateTraceCodeStatus(dto);

        // ---------- 验证状态 ----------
        TraceCode tc = traceCodeService.getByCode(testTraceCode);
        assertEquals(Integer.valueOf(3), tc.getTraceCodeStatus(), "状态应为「已禁用」");
        assertEquals("产品疑似质量问题，临时禁用", tc.getDisableReason());

        // ---------- 验证消费者扫码看到禁用提示 ----------
        ConsumerTraceVO scanResult = traceCodeService.queryByCode(testTraceCode);
        assertEquals("已禁用", scanResult.getCodeStatus());
        assertEquals("fail", scanResult.getQueryResult());
        assertTrue(scanResult.getRiskSuggestion().contains("禁用"));

        System.out.println("========== 溯源码禁用测试通过 ==========");
        System.out.println("溯源码: " + testTraceCode + " 已禁用");
        System.out.println("消费者看到: " + scanResult.getRiskSuggestion());
    }

    // ==================== 9. 状态变更 — 非法状态流转测试 ====================

    @Test
    @Order(9)
    @DisplayName("状态变更 — 非法流转应抛出异常")
    public void testIllegalStatusTransition() {
        // 需要一个新的溯源码来测试（因为 testTraceCode 已被禁用）
        TraceCodeGenerateDTO genDto = new TraceCodeGenerateDTO();
        genDto.setCodeType(1);
        genDto.setProductId("PROD_TEST");
        genDto.setProductName("测试产品");
        genDto.setEnterpriseId("ENT_TEST");
        genDto.setEnterpriseName("测试企业");
        genDto.setBatchNo("PBS20260623099");
        genDto.setQualityResult(1);
        genDto.setOperator("admin");
        TraceCodeVO vo = traceCodeService.generateTraceCode(genDto);
        String newCode = vo.getTraceCode();

        // 尝试从未绑定(1)直接禁用(3) — 应该失败，因为只有已激活(2)才能禁用
        TraceCodeStatusDTO dto = new TraceCodeStatusDTO();
        dto.setTraceCode(newCode);
        dto.setTargetStatus(3);  // 禁用
        dto.setReason("test");
        dto.setOperator("admin");

        assertThrows(RuntimeException.class, () -> {
            traceCodeService.updateTraceCodeStatus(dto);
        }, "从未绑定直接禁用应抛出异常");

        System.out.println("========== 非法状态流转测试通过 ==========");
        System.out.println("溯源码: " + newCode + " 状态: 已绑定 → 禁用被正确拒绝");
    }

    // ==================== 10. Hash 校验测试 ====================

    @Test
    @Order(10)
    @DisplayName("SHA-256 内容哈希校验")
    public void testContentHashVerification() {
        assertNotNull(testTraceCode, "需要先执行 testGenerateTraceCode");

        TraceCode tc = traceCodeService.getByCode(testTraceCode);
        assertNotNull(tc, "溯源码应存在");

        String originalHash = tc.getContentHash();
        String recalculated = traceCodeService.computeContentHash(tc);

        assertEquals(originalHash, recalculated,
            "重新计算的哈希应与存储的哈希一致");

        System.out.println("========== Hash校验测试通过 ==========");
        System.out.println("原始Hash: " + originalHash.substring(0, 16) + "...");
        System.out.println("重算Hash: " + recalculated.substring(0, 16) + "...");
        System.out.println("一致: 是 ✓");
    }

    // ==================== 11. 分页查询测试 ====================

    @Test
    @Order(11)
    @DisplayName("分页查询溯源码列表")
    public void testPaginationQuery() {
        TraceCodeQueryDTO dto = new TraceCodeQueryDTO();
        dto.setPage(1);
        dto.setPageSize(5);

        List<TraceCodeVO> list = traceCodeService.queryTraceCodeList(dto);
        assertNotNull(list);
        assertTrue(list.size() <= 5, "每页不超过5条");

        // 按条件查询
        TraceCodeQueryDTO filterDto = new TraceCodeQueryDTO();
        filterDto.setCodeType(1);  // 只查单品码
        filterDto.setPage(1);
        filterDto.setPageSize(10);
        List<TraceCodeVO> filteredList = traceCodeService.queryTraceCodeList(filterDto);
        for (TraceCodeVO vo : filteredList) {
            assertEquals("单品码", vo.getCodeTypeName(), "过滤结果应全是单品码");
        }

        System.out.println("========== 分页查询测试通过 ==========");
        System.out.println("第1页结果数: " + list.size());
        System.out.println("单品码过滤结果数: " + filteredList.size());
    }

    // ==================== 12. 业务数据绑定测试 ====================

    @Test
    @Order(12)
    @DisplayName("多类型业务数据绑定")
    public void testMultipleBusinessDataBinding() {
        // 生成新码用于绑定测试
        TraceCodeGenerateDTO genDto = new TraceCodeGenerateDTO();
        genDto.setCodeType(1);
        genDto.setProductId("PROD_BIND");
        genDto.setProductName("绑定测试产品");
        genDto.setEnterpriseId("ENT_BIND");
        genDto.setEnterpriseName("绑定测试企业");
        genDto.setBatchNo("PBS20260623100");
        genDto.setQualityResult(1);
        genDto.setOperator("admin");
        TraceCodeVO vo = traceCodeService.generateTraceCode(genDto);
        String code = vo.getTraceCode();

        // 绑定6种不同类型的业务数据
        String[][] bizData = {
            {"RAW_BATCH",      "RAW_001", "RB20260623100"},
            {"PROD_BATCH",     "PROD_001", "PBS20260623100"},
            {"PROCESS_BATCH",  "PROC_001", "PBJ20260623100"},
            {"INSPECTION",     "INSP_001", "QC20260623100"},
            {"LOGISTICS",      "LOG_001", "LO20260623100"},
            {"SALES_TERMINAL", "TERM_001", "ST20260623100"},
        };

        // 绑定所有6种业务数据
        for (String[] biz : bizData) {
            traceCodeService.bindBusinessData(code, biz[0], biz[1], biz[2], "admin");
        }

        // 验证绑定关系
        List<TraceCodeBind> binds = traceCodeService.listBindsByCode(code);
        assertEquals(6, binds.size(), "应绑定6条业务数据");
        System.out.println("========== 多类型业务数据绑定测试通过，绑定了 " + binds.size() + " 条 ==========");
    }

    // ==================== 13. 导出测试 ====================

    @Test
    @Order(13)
    @DisplayName("溯源码标签导出")
    public void testExportLabel() {
        assertNotNull(testTraceCode, "需要先执行 testGenerateTraceCode");

        TraceCodeExportDTO dto = new TraceCodeExportDTO();
        dto.setTraceCodeList(Arrays.asList(testTraceCode));
        dto.setTemplateId(1L);
        dto.setExportFormat("PDF");
        dto.setOperator("admin");

        String exportId = traceCodeService.exportTraceCodeLabel(dto);
        assertNotNull(exportId);
        assertTrue(exportId.startsWith("EXP_"));

        System.out.println("========== 导出测试通过 ==========");
        System.out.println("导出任务编号: " + exportId);

        // 测试空列表应抛异常
        TraceCodeExportDTO emptyDto = new TraceCodeExportDTO();
        emptyDto.setTraceCodeList(Arrays.asList());
        assertThrows(RuntimeException.class, () -> {
            traceCodeService.exportTraceCodeLabel(emptyDto);
        });
    }

    // ==================== 14. 质检不合格生成被拒测试 ====================

    @Test
    @Order(14)
    @DisplayName("质检不合格的产品不能生成溯源码")
    public void testRejectUnqualifiedProduct() {
        TraceCodeGenerateDTO dto = new TraceCodeGenerateDTO();
        dto.setCodeType(1);
        dto.setProductId("PROD_BAD");
        dto.setProductName("不合格产品");
        dto.setEnterpriseId("ENT_BAD");
        dto.setEnterpriseName("不合格企业");
        dto.setBatchNo("PBS20260623999");
        dto.setQualityResult(2);  // 不合格
        dto.setOperator("admin");

        assertThrows(RuntimeException.class, () -> {
            traceCodeService.generateTraceCode(dto);
        }, "质检不合格应拒绝生成溯源码");

        System.out.println("========== 不合格产品拒绝测试通过 ==========");
    }
}

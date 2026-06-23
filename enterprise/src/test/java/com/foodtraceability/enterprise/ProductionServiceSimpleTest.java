package com.foodtraceability.enterprise;

import com.foodtraceability.enterprise.entity.TechTemplate;
import com.foodtraceability.enterprise.entity.ProcessBatch;
import com.foodtraceability.enterprise.entity.ProdBatch;
import com.foodtraceability.enterprise.entity.ProdMaterialInput;
import com.foodtraceability.enterprise.entity.ProdEnvRecord;
import com.foodtraceability.enterprise.entity.QualityInspection;
import com.foodtraceability.enterprise.service.ProductionService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductionServiceSimpleTest {

    @Autowired
    private ProductionService productionService;

    // 用于跨测试方法共享数据
    private static Integer templateId;
    private static String templateName = "巴氏杀菌模板-测试";
    private static Integer processBatchId;
    private static String processBatchNo;
    private static Integer prodBatchId;
    private static String prodBatchNo;

    // ==================== t_tech_template ====================

    @Test
    @Order(1)
    public void testCreateTemplate() {
        try {
            TechTemplate template = new TechTemplate();
            template.setTemplateName(templateName);
            template.setVersion("V1.0");
            template.setApplicableProduct("鲜牛奶");
            template.setTargetTemp("72-75℃");
            template.setDuration("15秒");
            template.setPressure("20MPa");
            template.setCoolTemp("4℃");
            template.setFillTemp("6℃");
            template.setStirSpeed("120rpm");
            template.setPhValue("6.6-6.8");
            template.setViscosity("1.5-2.0");
            template.setCleanLevel(2);
            template.setTemplateStatus(1);
            template.setCreateBy("admin");
            template.setUpdateBy("admin");
            template.setIsDeleted(0);
            template.setRemark("测试工艺模板");

            int result = productionService.createTemplate(template);

            System.out.println("=== 创建工艺模板 ===");
            System.out.println("插入结果: " + result);
            System.out.println("模板ID: " + template.getTemplateId());
            System.out.println("模板名称: " + template.getTemplateName());
            System.out.println("适用产品: " + template.getApplicableProduct());
            System.out.println("版本: " + template.getVersion());
            System.out.println("创建时间: " + template.getCreateTime());

            assert result > 0 : "工艺模板创建失败";
            templateId = template.getTemplateId();
            System.out.println("[PASS] testCreateTemplate - 工艺模板创建成功, ID=" + templateId);
        } catch (Throwable e) {
            System.out.println("[FAIL] testCreateTemplate - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(2)
    public void testQueryTemplate() {
        try {
            TechTemplate template = productionService.getByTemplateName(templateName);

            System.out.println("=== 查询工艺模板 ===");
            System.out.println("模板ID: " + (template != null ? template.getTemplateId() : "null"));
            System.out.println("模板名称: " + (template != null ? template.getTemplateName() : "null"));
            System.out.println("杀菌温度: " + (template != null ? template.getTargetTemp() : "null"));

            assert template != null : "工艺模板查询失败";
            System.out.println("[PASS] testQueryTemplate - 工艺模板查询成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testQueryTemplate - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(3)
    public void testListTemplate() {
        try {
            List<TechTemplate> list = productionService.listTemplate("鲜牛奶", 1);

            System.out.println("=== 条件列表查询工艺模板 ===");
            System.out.println("查询结果数: " + list.size());
            for (TechTemplate t : list) {
                System.out.println("  - " + t.getTemplateName() + " | " + t.getApplicableProduct() + " | 状态:" + t.getTemplateStatus());
            }

            assert list.size() > 0 : "工艺模板列表不应为空";
            System.out.println("[PASS] testListTemplate - 查询到 " + list.size() + " 条工艺模板");
        } catch (Throwable e) {
            System.out.println("[FAIL] testListTemplate - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(4)
    public void testUpdateTemplate() {
        try {
            TechTemplate template = productionService.getByTemplateName(templateName);
            assert template != null : "更新前查询失败";

            template.setVersion("V1.1");
            template.setTargetTemp("73-76℃");
            template.setUpdateBy("admin");

            int result = productionService.updateTemplate(template);

            System.out.println("=== 更新工艺模板 ===");
            System.out.println("更新结果: " + result);
            System.out.println("新版本: " + template.getVersion());
            System.out.println("新杀菌温度: " + template.getTargetTemp());

            assert result > 0 : "工艺模板更新失败";
            System.out.println("[PASS] testUpdateTemplate - 工艺模板更新成功, 版本=" + template.getVersion());
        } catch (Throwable e) {
            System.out.println("[FAIL] testUpdateTemplate - " + e.getMessage());
            throw e;
        }
    }

    // ==================== t_process_batch ====================

    @Test
    @Order(5)
    public void testCreateProcessBatch() {
        try {
            ProcessBatch processBatch = new ProcessBatch();
            processBatch.setProductName("巴氏鲜牛奶-半成品");
            processBatch.setTemplateName(templateName);
            processBatch.setRawBatchNo("RB20260611008");
            processBatch.setPlannedAmount(10000);
            processBatch.setProcessDate("2026-06-23");
            processBatch.setOperator("张师傅");
            processBatch.setProductionLine("PL-01");
            processBatch.setShift(1);
            processBatch.setActualTemp("73.5℃");
            processBatch.setActualDuration("15秒");
            processBatch.setActualPressure("20.5MPa");
            processBatch.setActualCoolTemp("4.2℃");
            processBatch.setActualFillTemp("6.1℃");
            processBatch.setActualPh("6.7");
            processBatch.setActualViscosity("1.8");
            processBatch.setBatchStatus(1);
            processBatch.setDataHash("");
            processBatch.setChainHash("");
            processBatch.setCreateBy("admin");
            processBatch.setUpdateBy("admin");
            processBatch.setIsDeleted(0);
            processBatch.setRemark("测试加工批次");

            int result = productionService.createProcessBatch(processBatch);

            System.out.println("=== 创建加工批次 ===");
            System.out.println("插入结果: " + result);
            System.out.println("加工批次号: " + processBatch.getBatchNo());
            System.out.println("产品名称: " + processBatch.getProductName());
            System.out.println("生产线: " + processBatch.getProductionLine());
            System.out.println("操作员: " + processBatch.getOperator());
            System.out.println("创建时间: " + processBatch.getCreateTime());

            assert result > 0 : "加工批次创建失败";
            assert processBatch.getBatchNo() != null : "加工批次号未生成";
            processBatchId = processBatch.getProcessBatchId();
            processBatchNo = processBatch.getBatchNo();
            System.out.println("[PASS] testCreateProcessBatch - 加工批次创建成功, 批次号=" + processBatchNo);
        } catch (Throwable e) {
            System.out.println("[FAIL] testCreateProcessBatch - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(6)
    public void testQueryProcessBatch() {
        try {
            ProcessBatch processBatch = productionService.getByProcessBatchNo(processBatchNo);

            System.out.println("=== 查询加工批次 ===");
            System.out.println("加工批次ID: " + (processBatch != null ? processBatch.getProcessBatchId() : "null"));
            System.out.println("批次号: " + (processBatch != null ? processBatch.getBatchNo() : "null"));
            System.out.println("产品名称: " + (processBatch != null ? processBatch.getProductName() : "null"));
            System.out.println("状态: " + (processBatch != null ? processBatch.getBatchStatus() : "null"));

            assert processBatch != null : "加工批次查询失败";
            System.out.println("[PASS] testQueryProcessBatch - 加工批次查询成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testQueryProcessBatch - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(7)
    public void testListProcessBatch() {
        try {
            List<ProcessBatch> list = productionService.listProcessBatch(null, "PL-01", 1, null);

            System.out.println("=== 条件列表查询加工批次 ===");
            System.out.println("查询结果数: " + list.size());
            for (ProcessBatch p : list) {
                System.out.println("  - " + p.getBatchNo() + " | " + p.getProductName() + " | 状态:" + p.getBatchStatus());
            }

            assert list.size() > 0 : "加工批次列表不应为空";
            System.out.println("[PASS] testListProcessBatch - 查询到 " + list.size() + " 条加工批次");
        } catch (Throwable e) {
            System.out.println("[FAIL] testListProcessBatch - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(8)
    public void testCompleteProcessBatch() {
        try {
            int result = productionService.completeProcessBatch(processBatchId);

            System.out.println("=== 完成加工批次 ===");
            System.out.println("操作结果: " + result);

            ProcessBatch processBatch = productionService.getByProcessBatchNo(processBatchNo);
            System.out.println("加工批次状态: " + (processBatch != null ? processBatch.getBatchStatus() : "null"));

            assert result > 0 : "完成加工批次失败";
            System.out.println("[PASS] testCompleteProcessBatch - 加工批次已完成, 状态=" + (processBatch != null ? processBatch.getBatchStatus() : "null"));
        } catch (Throwable e) {
            System.out.println("[FAIL] testCompleteProcessBatch - " + e.getMessage());
            throw e;
        }
    }

    // ==================== t_prod_batch ====================

    @Test
    @Order(9)
    public void testCreateProdBatch() {
        try {
            ProdBatch prodBatch = new ProdBatch();
            prodBatch.setProductName("燕山鲜牛奶");
            prodBatch.setProcessBatchNo(processBatchNo);
            prodBatch.setProductionLine("PL-01");
            prodBatch.setPlannedAmount(10000);
            prodBatch.setActualAmount(9850);
            prodBatch.setProductionDate("2026-06-23");
            prodBatch.setCheckResult(2);
            prodBatch.setCodeStatus(0);
            prodBatch.setBatchStatus(1);
            prodBatch.setDataHash("");
            prodBatch.setChainHash("");
            prodBatch.setCreateBy("admin");
            prodBatch.setUpdateBy("admin");
            prodBatch.setIsDeleted(0);
            prodBatch.setRemark("测试生产批次");

            int result = productionService.createProdBatch(prodBatch);

            System.out.println("=== 创建生产批次 ===");
            System.out.println("插入结果: " + result);
            System.out.println("生产批次号: " + prodBatch.getBatchNo());
            System.out.println("产品名称: " + prodBatch.getProductName());
            System.out.println("计划产量: " + prodBatch.getPlannedAmount());
            System.out.println("实际产量: " + prodBatch.getActualAmount());
            System.out.println("创建时间: " + prodBatch.getCreateTime());

            assert result > 0 : "生产批次创建失败";
            assert prodBatch.getBatchNo() != null : "生产批次号未生成";
            prodBatchId = prodBatch.getProdBatchId();
            prodBatchNo = prodBatch.getBatchNo();
            System.out.println("[PASS] testCreateProdBatch - 生产批次创建成功, 批次号=" + prodBatchNo);
        } catch (Throwable e) {
            System.out.println("[FAIL] testCreateProdBatch - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(10)
    public void testQueryProdBatch() {
        try {
            ProdBatch prodBatch = productionService.getByProdBatchNo(prodBatchNo);

            System.out.println("=== 查询生产批次 ===");
            System.out.println("生产批次ID: " + (prodBatch != null ? prodBatch.getProdBatchId() : "null"));
            System.out.println("批次号: " + (prodBatch != null ? prodBatch.getBatchNo() : "null"));
            System.out.println("产品名称: " + (prodBatch != null ? prodBatch.getProductName() : "null"));
            System.out.println("质检结果: " + (prodBatch != null ? prodBatch.getCheckResult() : "null"));

            assert prodBatch != null : "生产批次查询失败";
            System.out.println("[PASS] testQueryProdBatch - 生产批次查询成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testQueryProdBatch - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(11)
    public void testListProdBatch() {
        try {
            List<ProdBatch> list = productionService.listProdBatch(null, "PL-01", null, null, null);

            System.out.println("=== 条件列表查询生产批次 ===");
            System.out.println("查询结果数: " + list.size());
            for (ProdBatch p : list) {
                System.out.println("  - " + p.getBatchNo() + " | " + p.getProductName() + " | 状态:" + p.getBatchStatus());
            }

            assert list.size() > 0 : "生产批次列表不应为空";
            System.out.println("[PASS] testListProdBatch - 查询到 " + list.size() + " 条生产批次");
        } catch (Throwable e) {
            System.out.println("[FAIL] testListProdBatch - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(12)
    public void testCompleteProdBatch() {
        try {
            int result = productionService.completeProdBatch(prodBatchId);

            System.out.println("=== 完成生产批次 ===");
            System.out.println("操作结果: " + result);

            ProdBatch prodBatch = productionService.getByProdBatchNo(prodBatchNo);
            System.out.println("生产批次状态: " + (prodBatch != null ? prodBatch.getBatchStatus() : "null"));

            assert result > 0 : "完成生产批次失败";
            System.out.println("[PASS] testCompleteProdBatch - 生产批次已完成, 状态=" + (prodBatch != null ? prodBatch.getBatchStatus() : "null"));
        } catch (Throwable e) {
            System.out.println("[FAIL] testCompleteProdBatch - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(13)
    public void testListProdByProcess() {
        try {
            List<ProdBatch> list = productionService.listByProcessBatchNo(processBatchNo);

            System.out.println("=== 按加工批次查询生产批次 ===");
            System.out.println("加工批次号: " + processBatchNo);
            System.out.println("关联生产批次数: " + list.size());
            for (ProdBatch p : list) {
                System.out.println("  - " + p.getBatchNo() + " | " + p.getProductName());
            }

            assert list.size() > 0 : "按加工批次查询不应为空";
            System.out.println("[PASS] testListProdByProcess - 加工批次 " + processBatchNo + " 关联 " + list.size() + " 条生产批次");
        } catch (Throwable e) {
            System.out.println("[FAIL] testListProdByProcess - " + e.getMessage());
            throw e;
        }
    }

    // ==================== t_prod_material_input ====================

    @Test
    @Order(14)
    public void testRecordMaterialInput() {
        try {
            ProdMaterialInput input = new ProdMaterialInput();
            input.setRawBatchNo("RB20260611008");
            input.setMaterialName("生鲜牛乳");
            input.setInputAmount(12000);
            input.setInputStatus("已投料");
            input.setCreateBy("admin");
            input.setUpdateBy("admin");
            input.setIsDeleted(0);

            int result = productionService.recordMaterialInput(input);

            System.out.println("=== 记录投料 ===");
            System.out.println("插入结果: " + result);
            System.out.println("投料ID: " + input.getInputId());
            System.out.println("原料批次号: " + input.getRawBatchNo());
            System.out.println("原料名称: " + input.getMaterialName());
            System.out.println("投料量: " + input.getInputAmount());

            assert result > 0 : "投料记录失败";
            System.out.println("[PASS] testRecordMaterialInput - 投料记录成功, 原料=" + input.getMaterialName());
        } catch (Throwable e) {
            System.out.println("[FAIL] testRecordMaterialInput - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(15)
    public void testQueryMaterialInput() {
        try {
            ProdMaterialInput input = productionService.getByRawBatchNo("RB20260611008");

            System.out.println("=== 查询投料记录 ===");
            System.out.println("投料ID: " + (input != null ? input.getInputId() : "null"));
            System.out.println("原料名称: " + (input != null ? input.getMaterialName() : "null"));
            System.out.println("投料量: " + (input != null ? input.getInputAmount() : "null"));
            System.out.println("投料状态: " + (input != null ? input.getInputStatus() : "null"));

            assert input != null : "投料记录查询失败";
            System.out.println("[PASS] testQueryMaterialInput - 投料记录查询成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testQueryMaterialInput - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(16)
    public void testListMaterialInput() {
        try {
            List<ProdMaterialInput> list = productionService.listMaterialInput("生鲜牛乳");

            System.out.println("=== 投料记录列表 ===");
            System.out.println("查询结果数: " + list.size());
            for (ProdMaterialInput m : list) {
                System.out.println("  - " + m.getRawBatchNo() + " | " + m.getMaterialName() + " | 投料量:" + m.getInputAmount());
            }

            assert list.size() > 0 : "投料记录列表不应为空";
            System.out.println("[PASS] testListMaterialInput - 查询到 " + list.size() + " 条投料记录");
        } catch (Throwable e) {
            System.out.println("[FAIL] testListMaterialInput - " + e.getMessage());
            throw e;
        }
    }

    // ==================== t_prod_env_record ====================

    @Test
    @Order(17)
    public void testRecordEnv() {
        try {
            ProdEnvRecord envRecord = new ProdEnvRecord();
            envRecord.setProductionLine("PL-01");
            envRecord.setCollector("李检验员");
            envRecord.setWorkshopTemp("22.5℃");
            envRecord.setWorkshopHumidity("55%");
            envRecord.setCleanLevel("万级");
            envRecord.setPressureDiff("15Pa");
            envRecord.setParticles("≤100/m³");
            envRecord.setBacteria("≤50/皿");
            envRecord.setIsAbnormal(0);
            envRecord.setCreateBy("admin");
            envRecord.setUpdateBy("admin");
            envRecord.setIsDeleted(0);
            envRecord.setRemark("测试环境采集");

            int result = productionService.recordEnv(envRecord);

            System.out.println("=== 采集环境数据 ===");
            System.out.println("插入结果: " + result);
            System.out.println("记录ID: " + envRecord.getEnvRecordId());
            System.out.println("生产线: " + envRecord.getProductionLine());
            System.out.println("车间温度: " + envRecord.getWorkshopTemp());
            System.out.println("车间湿度: " + envRecord.getWorkshopHumidity());
            System.out.println("洁净度: " + envRecord.getCleanLevel());
            System.out.println("采集时间: " + envRecord.getCollectTime());

            assert result > 0 : "环境数据采集失败";
            System.out.println("[PASS] testRecordEnv - 环境数据采集成功, 生产线=" + envRecord.getProductionLine());
        } catch (Throwable e) {
            System.out.println("[FAIL] testRecordEnv - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(18)
    public void testListEnvRecord() {
        try {
            List<ProdEnvRecord> list = productionService.listEnvRecordByLine("PL-01");

            System.out.println("=== 按生产线查询环境记录 ===");
            System.out.println("查询结果数: " + list.size());
            for (ProdEnvRecord e : list) {
                System.out.println("  - 时间:" + e.getCollectTime() + " | 温度:" + e.getWorkshopTemp()
                        + " | 湿度:" + e.getWorkshopHumidity() + " | 异常:" + e.getIsAbnormal());
            }

            assert list.size() > 0 : "环境记录列表不应为空";
            System.out.println("[PASS] testListEnvRecord - 查询到 " + list.size() + " 条环境记录");
        } catch (Throwable e) {
            System.out.println("[FAIL] testListEnvRecord - " + e.getMessage());
            throw e;
        }
    }

    // ==================== t_quality_inspection ====================

    @Test
    @Order(19)
    public void testCreateInspection() {
        try {
            QualityInspection inspection = new QualityInspection();
            inspection.setBizType(2);
            inspection.setBizBatchNo(prodBatchNo);
            inspection.setInspectionType(6);
            inspection.setInspectionResult(1);
            inspection.setInspector("王质检员");
            inspection.setStandard("GB 19301-2010");
            inspection.setInspectionDate("2026-06-23");
            inspection.setSensoryCheck(1);
            inspection.setMicrobeCheck(1);
            inspection.setSealCheck("密封完好");
            inspection.setDetailResult("感官检查正常，微生物检测合格，密封性良好");
            inspection.setCreateBy("admin");
            inspection.setUpdateBy("admin");
            inspection.setIsDeleted(0);
            inspection.setRemark("测试质检记录");

            int result = productionService.createInspection(inspection);

            System.out.println("=== 创建质检记录 ===");
            System.out.println("插入结果: " + result);
            System.out.println("检验编号: " + inspection.getInspectionNo());
            System.out.println("业务类型: " + inspection.getBizType());
            System.out.println("关联批次号: " + inspection.getBizBatchNo());
            System.out.println("检验结果: " + inspection.getInspectionResult());
            System.out.println("质检人: " + inspection.getInspector());
            System.out.println("创建时间: " + inspection.getCreateTime());

            assert result > 0 : "质检记录创建失败";
            assert inspection.getInspectionNo() != null : "检验编号未生成";
            System.out.println("[PASS] testCreateInspection - 质检记录创建成功, 检验编号=" + inspection.getInspectionNo());
        } catch (Throwable e) {
            System.out.println("[FAIL] testCreateInspection - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(20)
    public void testListInspectionByBiz() {
        try {
            List<QualityInspection> list = productionService.listByBizBatch(2, prodBatchNo);

            System.out.println("=== 按业务批次查询质检记录 ===");
            System.out.println("业务类型: 2 (生产加工)");
            System.out.println("批次号: " + prodBatchNo);
            System.out.println("查询结果数: " + list.size());
            for (QualityInspection q : list) {
                System.out.println("  - " + q.getInspectionNo() + " | 类型:" + q.getInspectionType()
                        + " | 结果:" + q.getInspectionResult() + " | 质检人:" + q.getInspector());
            }

            assert list.size() > 0 : "质检记录列表不应为空";
            System.out.println("[PASS] testListInspectionByBiz - 查询到 " + list.size() + " 条质检记录");
        } catch (Throwable e) {
            System.out.println("[FAIL] testListInspectionByBiz - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(21)
    public void testListInspection() {
        try {
            List<QualityInspection> list = productionService.listInspection(2, null, null, 1);

            System.out.println("=== 条件列表查询质检记录 ===");
            System.out.println("查询结果数: " + list.size());
            for (QualityInspection q : list) {
                System.out.println("  - " + q.getInspectionNo() + " | 业务:" + q.getBizType()
                        + " | 批次:" + q.getBizBatchNo() + " | 结果:" + q.getInspectionResult());
            }

            assert list.size() > 0 : "质检记录条件列表不应为空";
            System.out.println("[PASS] testListInspection - 查询到 " + list.size() + " 条合格质检记录");
        } catch (Throwable e) {
            System.out.println("[FAIL] testListInspection - " + e.getMessage());
            throw e;
        }
    }

    // ==================== 质检结果联动 ====================

    @Test
    @Order(22)
    public void testQualityCheckProd() {
        try {
            productionService.recordQualityCheckForProd(prodBatchNo, 1);

            ProdBatch prodBatch = productionService.getByProdBatchNo(prodBatchNo);

            System.out.println("=== 生产批次质检联动 ===");
            System.out.println("批次号: " + prodBatchNo);
            System.out.println("质检结果: " + (prodBatch != null ? prodBatch.getCheckResult() : "null"));
            System.out.println("批次状态: " + (prodBatch != null ? prodBatch.getBatchStatus() : "null"));

            assert prodBatch != null : "生产批次查询失败";
            assert prodBatch.getCheckResult() == 1 : "质检结果应为合格(1)";
            System.out.println("[PASS] testQualityCheckProd - 质检联动成功, 质检结果=合格, 批次状态=" + prodBatch.getBatchStatus());
        } catch (Throwable e) {
            System.out.println("[FAIL] testQualityCheckProd - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(23)
    public void testQualityCheckProcess() {
        try {
            productionService.recordQualityCheckForProcess(processBatchNo, 1);

            ProcessBatch processBatch = productionService.getByProcessBatchNo(processBatchNo);

            System.out.println("=== 加工批次质检联动 ===");
            System.out.println("批次号: " + processBatchNo);
            System.out.println("批次状态: " + (processBatch != null ? processBatch.getBatchStatus() : "null"));

            assert processBatch != null : "加工批次查询失败";
            System.out.println("[PASS] testQualityCheckProcess - 加工批次质检联动成功, 状态=" + processBatch.getBatchStatus());
        } catch (Throwable e) {
            System.out.println("[FAIL] testQualityCheckProcess - " + e.getMessage());
            throw e;
        }
    }

    // ==================== 生产全链路追溯 ====================

    @Test
    @Order(24)
    public void testTraceProcessChain() {
        try {
            ProcessBatch processBatch = productionService.traceProcessChain(prodBatchNo);

            System.out.println("=== 生产全链路追溯 ===");
            System.out.println("生产批次号: " + prodBatchNo);
            if (processBatch != null) {
                System.out.println("  → 加工批次号: " + processBatch.getBatchNo());
                System.out.println("  → 加工产品: " + processBatch.getProductName());
                System.out.println("  → 原料批次号: " + processBatch.getRawBatchNo());
                System.out.println("  → 生产线: " + processBatch.getProductionLine());
            } else {
                System.out.println("  → 未找到上游加工批次");
            }

            assert processBatch != null : "追溯全链路失败";
            System.out.println("[PASS] testTraceProcessChain - 追溯成功, 生产→加工→原料: " + processBatch.getRawBatchNo());
        } catch (Throwable e) {
            System.out.println("[FAIL] testTraceProcessChain - " + e.getMessage());
            throw e;
        }
    }

    // ==================== 删除（软删除） ====================

    @Test
    @Order(98)
    public void testDeleteProdBatch() {
        try {
            int result = productionService.deleteProdBatch(prodBatchId);

            System.out.println("=== 软删除生产批次 ===");
            System.out.println("操作结果: " + result);

            ProdBatch prodBatch = productionService.getByProdBatchNo(prodBatchNo);
            System.out.println("删除后查询: " + (prodBatch == null ? "已删除(过滤)" : "仍存在"));

            assert result > 0 : "生产批次软删除失败";
            System.out.println("[PASS] testDeleteProdBatch - 生产批次软删除成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testDeleteProdBatch - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(99)
    public void testDeleteProcessBatch() {
        try {
            int result = productionService.deleteProcessBatch(processBatchId);

            System.out.println("=== 软删除加工批次 ===");
            System.out.println("操作结果: " + result);

            assert result > 0 : "加工批次软删除失败";
            System.out.println("[PASS] testDeleteProcessBatch - 加工批次软删除成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testDeleteProcessBatch - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(100)
    public void testDeleteTemplate() {
        try {
            int result = productionService.deleteTemplate(templateId);

            System.out.println("=== 软删除工艺模板 ===");
            System.out.println("操作结果: " + result);

            assert result > 0 : "工艺模板软删除失败";
            System.out.println("[PASS] testDeleteTemplate - 工艺模板软删除成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testDeleteTemplate - " + e.getMessage());
            throw e;
        }
    }
}

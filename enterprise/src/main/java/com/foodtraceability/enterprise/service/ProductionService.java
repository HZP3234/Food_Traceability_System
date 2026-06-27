package com.foodtraceability.enterprise.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.foodtraceability.enterprise.entity.TechTemplate;
import com.foodtraceability.enterprise.entity.ProdBatch;
import com.foodtraceability.enterprise.entity.ProdMaterialInput;
import com.foodtraceability.enterprise.entity.ProdEnvRecord;
import com.foodtraceability.enterprise.entity.QualityInspection;
import com.foodtraceability.enterprise.entity.Raw;
import com.foodtraceability.enterprise.mapper.TechTemplateMapper;
import com.foodtraceability.enterprise.mapper.EnterpriseProdBatchMapper;
import com.foodtraceability.enterprise.mapper.ProdMaterialInputMapper;
import com.foodtraceability.enterprise.mapper.ProdEnvRecordMapper;
import com.foodtraceability.enterprise.mapper.EnterpriseQualityInspectionMapper;
import com.foodtraceability.enterprise.mapper.RawMapper;
import com.foodtraceability.enterprise.util.CurrentUserUtil;

/**
 * 生产管理 Service — 加工批次已合并到生产批次
 * 链路：原料批次(raw_batch_no) → 生产批次(含加工参数) → 冷链/销售
 */
@Service
public class ProductionService {

    @Autowired
    private TechTemplateMapper techTemplateMapper;
    @Autowired
    private EnterpriseProdBatchMapper prodBatchMapper;
    @Autowired
    private ProdMaterialInputMapper prodMaterialInputMapper;
    @Autowired
    private ProdEnvRecordMapper prodEnvRecordMapper;
    @Autowired
    private EnterpriseQualityInspectionMapper qualityInspectionMapper;
    @Autowired
    private RawMapper rawMapper;
    @Autowired
    private CurrentUserUtil currentUserUtil;

    // 生成生产批次号 PBS + yyyyMMdd + 4位序号
    public String generateProdBatchNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seqPart = String.format("%04d", (System.currentTimeMillis() / 1000) % 10000);
        return "PBS" + datePart + seqPart;
    }

    // 生成质检编号 QI + yyyyMMdd + 4位序号
    public String generateInspectionNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seqPart = String.format("%04d", (System.currentTimeMillis() / 1000) % 10000);
        return "QI" + datePart + seqPart;
    }

    // ==================== t_tech_template ====================

    public TechTemplate getByTemplateName(String templateName) {
        QueryWrapper<TechTemplate> qw = new QueryWrapper<>();
        qw.eq("template_name", templateName);
        qw.eq("is_deleted", 0);
        // 生产加工商只能查看自己创建的工艺模板
        if (currentUserUtil.isManufacturer()) {
            qw.eq("create_by", currentUserUtil.getCurrentUsername());
        }
        return techTemplateMapper.selectOne(qw);
    }

    public List<TechTemplate> listByApplicableProduct(String applicableProduct) {
        QueryWrapper<TechTemplate> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        // 生产加工商只能查看自己创建的工艺模板
        if (currentUserUtil.isManufacturer()) {
            qw.eq("create_by", currentUserUtil.getCurrentUsername());
        }
        if (applicableProduct != null && !applicableProduct.isBlank())
            qw.like("applicable_product", applicableProduct);
        qw.orderByDesc("create_time");
        return techTemplateMapper.selectList(qw);
    }

    public List<TechTemplate> listTemplate(String applicableProduct, Integer templateStatus) {
        QueryWrapper<TechTemplate> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        // 生产加工商只能查看自己创建的工艺模板
        if (currentUserUtil.isManufacturer()) {
            qw.eq("create_by", currentUserUtil.getCurrentUsername());
        }
        if (applicableProduct != null && !applicableProduct.isBlank())
            qw.like("applicable_product", applicableProduct);
        if (templateStatus != null)
            qw.eq("template_status", templateStatus);
        qw.orderByDesc("create_time");
        return techTemplateMapper.selectList(qw);
    }

    public int createTemplate(TechTemplate template) {
        if (template.getVersion() == null || template.getVersion().isBlank()) {
            template.setVersion("V1.0");
        }
        if (template.getTemplateStatus() == 0) template.setTemplateStatus(1);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        template.setCreateTime(now);
        template.setUpdateTime(now);
        template.setCreateBy(currentUserUtil.getCurrentUsername());
        template.setUpdateBy(currentUserUtil.getCurrentUsername());
        if (template.getPressure() == null) template.setPressure("");
        if (template.getCoolTemp() == null) template.setCoolTemp("");
        if (template.getFillTemp() == null) template.setFillTemp("");
        if (template.getStirSpeed() == null) template.setStirSpeed("");
        if (template.getPhValue() == null) template.setPhValue("");
        if (template.getViscosity() == null) template.setViscosity("");
        return techTemplateMapper.insert(template);
    }

    public int updateTemplate(TechTemplate template) {
        template.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return techTemplateMapper.updateById(template);
    }

    public int deleteTemplate(Long templateId) {
        TechTemplate template = techTemplateMapper.selectById(templateId);
        if (template != null) {
            template.setIsDeleted(1);
            return techTemplateMapper.updateById(template);
        }
        return 0;
    }

    // ==================== t_prod_batch（已合并加工字段） ====================

    public ProdBatch getByProdBatchNo(String batchNo) {
        QueryWrapper<ProdBatch> qw = new QueryWrapper<>();
        qw.eq("batch_no", batchNo);
        qw.eq("is_deleted", 0);
        // 生产加工商只能查看自己创建的生产批次
        if (currentUserUtil.isManufacturer()) {
            qw.eq("create_by", currentUserUtil.getCurrentUsername());
        }
        return prodBatchMapper.selectOne(qw);
    }

    /** 按原料批次号查询生产批次 */
    public List<ProdBatch> listByRawBatchNo(String rawBatchNo) {
        QueryWrapper<ProdBatch> qw = new QueryWrapper<>();
        qw.eq("raw_batch_no", rawBatchNo);
        qw.eq("is_deleted", 0);
        // 生产加工商只能查看自己创建的生产批次
        if (currentUserUtil.isManufacturer()) {
            qw.eq("create_by", currentUserUtil.getCurrentUsername());
        }
        qw.orderByDesc("create_time");
        return prodBatchMapper.selectList(qw);
    }

    /** 条件列表查询 */
    public List<ProdBatch> listProdBatch(String productName, String productionLine,
                                          Integer checkResult, Integer batchStatus,
                                          Integer codeStatus, String rawBatchNo) {
        QueryWrapper<ProdBatch> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        // 生产加工商只能查看自己创建的生产批次
        if (currentUserUtil.isManufacturer()) {
            qw.eq("create_by", currentUserUtil.getCurrentUsername());
        }
        if (productName != null && !productName.isBlank())
            qw.eq("product_name", productName);
        if (productionLine != null && !productionLine.isBlank())
            qw.eq("production_line", productionLine);
        if (checkResult != null)
            qw.eq("check_result", checkResult);
        if (batchStatus != null)
            qw.eq("batch_status", batchStatus);
        if (codeStatus != null)
            qw.eq("code_status", codeStatus);
        if (rawBatchNo != null && !rawBatchNo.isBlank())
            qw.eq("raw_batch_no", rawBatchNo);
        qw.orderByDesc("create_time");
        return prodBatchMapper.selectList(qw);
    }

    /** 新增生产批次（含加工参数） */
    public int createProdBatch(ProdBatch prodBatch) {
        if (prodBatch.getBatchNo() == null || prodBatch.getBatchNo().isBlank()) {
            prodBatch.setBatchNo(generateProdBatchNo());
        }
        if (prodBatch.getCheckResult() == null) prodBatch.setCheckResult(0);
        if (prodBatch.getBatchStatus() == null) prodBatch.setBatchStatus(1);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        prodBatch.setCreateTime(now);
        prodBatch.setUpdateTime(now);
        prodBatch.setCreateBy(currentUserUtil.getCurrentUsername());
        prodBatch.setUpdateBy(currentUserUtil.getCurrentUsername());
        if (prodBatch.getDataHash() == null) prodBatch.setDataHash("");
        if (prodBatch.getChainHash() == null) prodBatch.setChainHash("");
        // 加工字段默认值
        if (prodBatch.getTemplateName() == null) prodBatch.setTemplateName("");
        if (prodBatch.getRawBatchNo() == null) prodBatch.setRawBatchNo("");
        if (prodBatch.getProcessDate() == null || prodBatch.getProcessDate().isBlank())
            prodBatch.setProcessDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        if (prodBatch.getOperator() == null) prodBatch.setOperator("");
        if (prodBatch.getActualTemp() == null) prodBatch.setActualTemp("");
        if (prodBatch.getActualDuration() == null) prodBatch.setActualDuration("");
        if (prodBatch.getActualPressure() == null) prodBatch.setActualPressure("");
        if (prodBatch.getActualCoolTemp() == null) prodBatch.setActualCoolTemp("");
        if (prodBatch.getActualFillTemp() == null) prodBatch.setActualFillTemp("");
        if (prodBatch.getActualPh() == null) prodBatch.setActualPh("");
        if (prodBatch.getActualViscosity() == null) prodBatch.setActualViscosity("");
        if (prodBatch.getRemark() == null) prodBatch.setRemark("");
        return prodBatchMapper.insert(prodBatch);
    }

    /** 更新生产批次 */
    public int updateProdBatch(ProdBatch prodBatch) {
        prodBatch.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return prodBatchMapper.updateById(prodBatch);
    }

    /** 开始生产（待生产 → 生产中） */
    public int startProdBatch(Long prodBatchId) {
        ProdBatch prodBatch = prodBatchMapper.selectById(prodBatchId);
        if (prodBatch != null && prodBatch.getBatchStatus() != null && prodBatch.getBatchStatus() == 1) {
            prodBatch.setBatchStatus(2);
            prodBatch.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return prodBatchMapper.updateById(prodBatch);
        }
        return 0;
    }

    /** 完成生产 */
    public int completeProdBatch(Long prodBatchId) {
        ProdBatch prodBatch = prodBatchMapper.selectById(prodBatchId);
        if (prodBatch != null) {
            prodBatch.setBatchStatus(3);
            prodBatch.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return prodBatchMapper.updateById(prodBatch);
        }
        return 0;
    }

    /** 绑码完成 */
    public int bindCodeComplete(Long prodBatchId) {
        ProdBatch prodBatch = prodBatchMapper.selectById(prodBatchId);
        if (prodBatch != null) {
            prodBatch.setCodeStatus(1);
            prodBatch.setBatchStatus(3);
            prodBatch.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return prodBatchMapper.updateById(prodBatch);
        }
        return 0;
    }

    /** 软删除 */
    public int deleteProdBatch(Long prodBatchId) {
        ProdBatch prodBatch = prodBatchMapper.selectById(prodBatchId);
        if (prodBatch != null) {
            prodBatch.setIsDeleted(1);
            return prodBatchMapper.updateById(prodBatch);
        }
        return 0;
    }

    // ==================== t_prod_material_input ====================

    public ProdMaterialInput getByRawBatchNo(String rawBatchNo) {
        QueryWrapper<ProdMaterialInput> qw = new QueryWrapper<>();
        qw.eq("raw_batch_no", rawBatchNo);
        qw.eq("is_deleted", 0);
        // 生产加工商只能查看自己的投料记录
        if (currentUserUtil.isManufacturer()) {
            qw.eq("create_by", currentUserUtil.getCurrentUsername());
        }
        return prodMaterialInputMapper.selectOne(qw);
    }

    public List<ProdMaterialInput> listMaterialInput(String materialName) {
        QueryWrapper<ProdMaterialInput> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        // 生产加工商只能查看自己的投料记录
        if (currentUserUtil.isManufacturer()) {
            qw.eq("create_by", currentUserUtil.getCurrentUsername());
        }
        if (materialName != null && !materialName.isBlank())
            qw.eq("material_name", materialName);
        qw.orderByDesc("create_time");
        return prodMaterialInputMapper.selectList(qw);
    }

    public int recordMaterialInput(ProdMaterialInput input) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        input.setCreateTime(now);
        input.setUpdateTime(now);
        input.setCreateBy(currentUserUtil.getCurrentUsername());
        input.setUpdateBy(currentUserUtil.getCurrentUsername());
        return prodMaterialInputMapper.insert(input);
    }

    public int updateMaterialInput(ProdMaterialInput input) {
        input.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return prodMaterialInputMapper.updateById(input);
    }

    // listEnvRecordByAbnormal 保留供预警页面使用
    public List<ProdEnvRecord> listEnvRecordByAbnormal(int isAbnormal) {
        QueryWrapper<ProdEnvRecord> qw = new QueryWrapper<>();
        qw.eq("is_abnormal", isAbnormal);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("collect_time");
        return prodEnvRecordMapper.selectList(qw);
    }

    // ==================== t_quality_inspection ====================

    public QualityInspection getByInspectionNo(String inspectionNo) {
        QueryWrapper<QualityInspection> qw = new QueryWrapper<>();
        qw.eq("inspection_no", inspectionNo);
        qw.eq("is_deleted", 0);
        // 生产加工商只能查看自己的质检记录
        if (currentUserUtil.isManufacturer()) {
            qw.eq("create_by", currentUserUtil.getCurrentUsername());
        }
        return qualityInspectionMapper.selectOne(qw);
    }

    public List<QualityInspection> listByBizBatch(int bizType, String bizBatchNo) {
        QueryWrapper<QualityInspection> qw = new QueryWrapper<>();
        qw.eq("biz_type", bizType);
        qw.eq("biz_batch_no", bizBatchNo);
        qw.eq("is_deleted", 0);
        // 生产加工商只能查看自己的质检记录
        if (currentUserUtil.isManufacturer()) {
            qw.eq("create_by", currentUserUtil.getCurrentUsername());
        }
        qw.orderByDesc("create_time");
        return qualityInspectionMapper.selectList(qw);
    }

    public List<QualityInspection> listByBizType(int bizType) {
        QueryWrapper<QualityInspection> qw = new QueryWrapper<>();
        qw.eq("biz_type", bizType);
        qw.eq("is_deleted", 0);
        // 生产加工商只能查看自己的质检记录
        if (currentUserUtil.isManufacturer()) {
            qw.eq("create_by", currentUserUtil.getCurrentUsername());
        }
        qw.orderByDesc("create_time");
        return qualityInspectionMapper.selectList(qw);
    }

    public List<QualityInspection> listInspection(Integer bizType, String bizBatchNo,
                                                   Integer inspectionType, Integer inspectionResult) {
        QueryWrapper<QualityInspection> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        // 生产加工商只能查看自己的质检记录
        if (currentUserUtil.isManufacturer()) {
            qw.eq("create_by", currentUserUtil.getCurrentUsername());
        }
        if (bizType != null)
            qw.eq("biz_type", bizType);
        if (bizBatchNo != null && !bizBatchNo.isBlank())
            qw.eq("biz_batch_no", bizBatchNo);
        if (inspectionType != null)
            qw.eq("inspection_type", inspectionType);
        if (inspectionResult != null)
            qw.eq("inspection_result", inspectionResult);
        qw.orderByDesc("create_time");
        return qualityInspectionMapper.selectList(qw);
    }

    public int createInspection(QualityInspection inspection) {
        if (inspection.getInspectionNo() == null || inspection.getInspectionNo().isBlank()) {
            inspection.setInspectionNo(generateInspectionNo());
        }
        if (inspection.getInspectionResult() == null) inspection.setInspectionResult(3);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        inspection.setCreateTime(now);
        inspection.setUpdateTime(now);
        inspection.setCreateBy(currentUserUtil.getCurrentUsername());
        inspection.setUpdateBy(currentUserUtil.getCurrentUsername());
        if (inspection.getInspector() == null) inspection.setInspector("");
        if (inspection.getRemark() == null) inspection.setRemark("");
        return qualityInspectionMapper.insert(inspection);
    }

    public int updateInspection(QualityInspection inspection) {
        inspection.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return qualityInspectionMapper.updateById(inspection);
    }

    // ==================== 质检结果联动 ====================

    /** 录入生产批次质检结果 */
    public void recordQualityCheckForProd(String prodBatchNo, int checkResult) {
        ProdBatch prodBatch = getByProdBatchNo(prodBatchNo);
        if (prodBatch != null) {
            prodBatch.setCheckResult(checkResult);
            prodBatch.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            prodBatchMapper.updateById(prodBatch);
        }
    }

    // ==================== 生产全链路追溯 ====================

    /**
     * 生产全链路追溯结果 VO — 从生产批次直接追溯到原料批次
     * （加工批次已合并到生产批次）
     */
    public static class ProductionChainTraceVO {
        private ProdBatch prodBatch;
        private Raw rawBatch;
        private List<ProdMaterialInput> materialInputs;

        public ProdBatch getProdBatch() { return prodBatch; }
        public void setProdBatch(ProdBatch prodBatch) { this.prodBatch = prodBatch; }
        public Raw getRawBatch() { return rawBatch; }
        public void setRawBatch(Raw rawBatch) { this.rawBatch = rawBatch; }
        public List<ProdMaterialInput> getMaterialInputs() { return materialInputs; }
        public void setMaterialInputs(List<ProdMaterialInput> materialInputs) { this.materialInputs = materialInputs; }
    }

    /**
     * 根据生产批次号追溯全链路：生产批次 → 原料批次
     */
    public ProductionChainTraceVO traceProcessChain(String prodBatchNo) {
        ProdBatch prodBatch = getByProdBatchNo(prodBatchNo);
        if (prodBatch == null) return null;

        ProductionChainTraceVO vo = new ProductionChainTraceVO();
        vo.setProdBatch(prodBatch);

        // 直接从生产批次追溯到原料批次
        if (prodBatch.getRawBatchNo() != null && !prodBatch.getRawBatchNo().isBlank()) {
            QueryWrapper<Raw> qw = new QueryWrapper<>();
            qw.eq("batch_no", prodBatch.getRawBatchNo());
            qw.eq("is_deleted", 0);
            Raw raw = rawMapper.selectOne(qw);
            vo.setRawBatch(raw);
        }

        // 查询投料记录
        List<ProdMaterialInput> inputs = listMaterialInput(prodBatch.getProductName());
        vo.setMaterialInputs(inputs);

        return vo;
    }
}

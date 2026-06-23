package com.foodtraceability.enterprise.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.foodtraceability.enterprise.entity.TechTemplate;
import com.foodtraceability.enterprise.entity.ProcessBatch;
import com.foodtraceability.enterprise.entity.ProdBatch;
import com.foodtraceability.enterprise.entity.ProdMaterialInput;
import com.foodtraceability.enterprise.entity.ProdEnvRecord;
import com.foodtraceability.enterprise.entity.QualityInspection;
import com.foodtraceability.enterprise.mapper.TechTemplateMapper;
import com.foodtraceability.enterprise.mapper.ProcessBatchMapper;
import com.foodtraceability.enterprise.mapper.ProdBatchMapper;
import com.foodtraceability.enterprise.mapper.ProdMaterialInputMapper;
import com.foodtraceability.enterprise.mapper.ProdEnvRecordMapper;
import com.foodtraceability.enterprise.mapper.QualityInspectionMapper;

@Service
public class ProductionService {
    @Autowired
    private TechTemplateMapper techTemplateMapper;
    @Autowired
    private ProcessBatchMapper processBatchMapper;
    @Autowired
    private ProdBatchMapper prodBatchMapper;
    @Autowired
    private ProdMaterialInputMapper prodMaterialInputMapper;
    @Autowired
    private ProdEnvRecordMapper prodEnvRecordMapper;
    @Autowired
    private QualityInspectionMapper qualityInspectionMapper;

    // 生成加工批次号 PBJ + yyyyMMdd + 4位序号
    public String generateProcessBatchNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seqPart = String.format("%04d", System.currentTimeMillis() % 10000);
        return "PBJ" + datePart + seqPart;
    }

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

    // 按模板名称查询
    public TechTemplate getByTemplateName(String templateName) {
        QueryWrapper<TechTemplate> qw = new QueryWrapper<>();
        qw.eq("template_name", templateName);
        qw.eq("is_deleted", 0);
        return techTemplateMapper.selectOne(qw);
    }

    // 按适用产品查询模板列表
    public List<TechTemplate> listByApplicableProduct(String applicableProduct) {
        QueryWrapper<TechTemplate> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        if (applicableProduct != null && !applicableProduct.isBlank())
            qw.eq("applicable_product", applicableProduct);
        qw.orderByDesc("create_time");
        return techTemplateMapper.selectList(qw);
    }

    // 条件列表查询
    public List<TechTemplate> listTemplate(String applicableProduct, Integer templateStatus) {
        QueryWrapper<TechTemplate> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        if (applicableProduct != null && !applicableProduct.isBlank())
            qw.eq("applicable_product", applicableProduct);
        if (templateStatus != null)
            qw.eq("template_status", templateStatus);
        qw.orderByDesc("create_time");
        return techTemplateMapper.selectList(qw);
    }

    // 新增工艺模板
    public int createTemplate(TechTemplate template) {
        if (template.getVersion() == null || template.getVersion().isBlank()) {
            template.setVersion("V1.0");
        }
        if (template.getTemplateStatus() == 0) template.setTemplateStatus(1);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        template.setCreateTime(now);
        template.setUpdateTime(now);
        return techTemplateMapper.insert(template);
    }

    // 更新工艺模板
    public int updateTemplate(TechTemplate template) {
        template.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return techTemplateMapper.updateById(template);
    }

    // 软删除工艺模板
    public int deleteTemplate(Integer templateId) {
        TechTemplate template = techTemplateMapper.selectById(templateId);
        if (template != null) {
            template.setIsDeleted(1);
            return techTemplateMapper.updateById(template);
        }
        return 0;
    }

    // ==================== t_process_batch ====================

    // 按加工批次号查询
    public ProcessBatch getByProcessBatchNo(String batchNo) {
        QueryWrapper<ProcessBatch> qw = new QueryWrapper<>();
        qw.eq("batch_no", batchNo);
        qw.eq("is_deleted", 0);
        return processBatchMapper.selectOne(qw);
    }

    // 按原料批次号查询加工批次
    public List<ProcessBatch> listByRawBatchNo(String rawBatchNo) {
        QueryWrapper<ProcessBatch> qw = new QueryWrapper<>();
        qw.eq("raw_batch_no", rawBatchNo);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("create_time");
        return processBatchMapper.selectList(qw);
    }

    // 条件列表查询
    public List<ProcessBatch> listProcessBatch(String productName, String productionLine,
                                                Integer batchStatus, Integer shift) {
        QueryWrapper<ProcessBatch> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        if (productName != null && !productName.isBlank())
            qw.eq("product_name", productName);
        if (productionLine != null && !productionLine.isBlank())
            qw.eq("production_line", productionLine);
        if (batchStatus != null)
            qw.eq("batch_status", batchStatus);
        if (shift != null)
            qw.eq("shift", shift);
        qw.orderByDesc("create_time");
        return processBatchMapper.selectList(qw);
    }

    // 新增加工批次
    public int createProcessBatch(ProcessBatch processBatch) {
        if (processBatch.getBatchNo() == null || processBatch.getBatchNo().isBlank()) {
            processBatch.setBatchNo(generateProcessBatchNo());
        }
        if (processBatch.getBatchStatus() == 0) processBatch.setBatchStatus(1);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        processBatch.setCreateTime(now);
        processBatch.setUpdateTime(now);
        return processBatchMapper.insert(processBatch);
    }

    // 更新加工批次
    public int updateProcessBatch(ProcessBatch processBatch) {
        processBatch.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return processBatchMapper.updateById(processBatch);
    }

    // 完成加工批次
    public int completeProcessBatch(Integer processBatchId) {
        ProcessBatch processBatch = processBatchMapper.selectById(processBatchId);
        if (processBatch != null) {
            processBatch.setBatchStatus(2);
            processBatch.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return processBatchMapper.updateById(processBatch);
        }
        return 0;
    }

    // 软删除加工批次
    public int deleteProcessBatch(Integer processBatchId) {
        ProcessBatch processBatch = processBatchMapper.selectById(processBatchId);
        if (processBatch != null) {
            processBatch.setIsDeleted(1);
            return processBatchMapper.updateById(processBatch);
        }
        return 0;
    }

    // ==================== t_prod_batch ====================

    // 按生产批次号查询
    public ProdBatch getByProdBatchNo(String batchNo) {
        QueryWrapper<ProdBatch> qw = new QueryWrapper<>();
        qw.eq("batch_no", batchNo);
        qw.eq("is_deleted", 0);
        return prodBatchMapper.selectOne(qw);
    }

    // 按加工批次号查询生产批次
    public List<ProdBatch> listByProcessBatchNo(String processBatchNo) {
        QueryWrapper<ProdBatch> qw = new QueryWrapper<>();
        qw.eq("process_batch_no", processBatchNo);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("create_time");
        return prodBatchMapper.selectList(qw);
    }

    // 条件列表查询
    public List<ProdBatch> listProdBatch(String productName, String productionLine,
                                          Integer checkResult, Integer batchStatus,
                                          Integer codeStatus) {
        QueryWrapper<ProdBatch> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
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
        qw.orderByDesc("create_time");
        return prodBatchMapper.selectList(qw);
    }

    // 新增生产批次
    public int createProdBatch(ProdBatch prodBatch) {
        if (prodBatch.getBatchNo() == null || prodBatch.getBatchNo().isBlank()) {
            prodBatch.setBatchNo(generateProdBatchNo());
        }
        if (prodBatch.getCheckResult() == 0) prodBatch.setCheckResult(2);
        if (prodBatch.getBatchStatus() == 0) prodBatch.setBatchStatus(1);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        prodBatch.setCreateTime(now);
        prodBatch.setUpdateTime(now);
        return prodBatchMapper.insert(prodBatch);
    }

    // 更新生产批次
    public int updateProdBatch(ProdBatch prodBatch) {
        prodBatch.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return prodBatchMapper.updateById(prodBatch);
    }

    // 完成生产批次
    public int completeProdBatch(Integer prodBatchId) {
        ProdBatch prodBatch = prodBatchMapper.selectById(prodBatchId);
        if (prodBatch != null) {
            prodBatch.setBatchStatus(2);
            prodBatch.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return prodBatchMapper.updateById(prodBatch);
        }
        return 0;
    }

    // 绑码完成
    public int bindCodeComplete(Integer prodBatchId) {
        ProdBatch prodBatch = prodBatchMapper.selectById(prodBatchId);
        if (prodBatch != null) {
            prodBatch.setCodeStatus(1);
            prodBatch.setBatchStatus(3);
            prodBatch.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return prodBatchMapper.updateById(prodBatch);
        }
        return 0;
    }

    // 软删除生产批次
    public int deleteProdBatch(Integer prodBatchId) {
        ProdBatch prodBatch = prodBatchMapper.selectById(prodBatchId);
        if (prodBatch != null) {
            prodBatch.setIsDeleted(1);
            return prodBatchMapper.updateById(prodBatch);
        }
        return 0;
    }

    // ==================== t_prod_material_input ====================

    // 按原料批次号查询投料记录
    public ProdMaterialInput getByRawBatchNo(String rawBatchNo) {
        QueryWrapper<ProdMaterialInput> qw = new QueryWrapper<>();
        qw.eq("raw_batch_no", rawBatchNo);
        qw.eq("is_deleted", 0);
        return prodMaterialInputMapper.selectOne(qw);
    }

    // 投料记录列表
    public List<ProdMaterialInput> listMaterialInput(String materialName) {
        QueryWrapper<ProdMaterialInput> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        if (materialName != null && !materialName.isBlank())
            qw.eq("material_name", materialName);
        qw.orderByDesc("create_time");
        return prodMaterialInputMapper.selectList(qw);
    }

    // 记录投料
    public int recordMaterialInput(ProdMaterialInput input) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        input.setCreateTime(now);
        input.setUpdateTime(now);
        return prodMaterialInputMapper.insert(input);
    }

    // 更新投料记录
    public int updateMaterialInput(ProdMaterialInput input) {
        input.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return prodMaterialInputMapper.updateById(input);
    }

    // ==================== t_prod_env_record ====================

    // 按生产线查询环境记录
    public List<ProdEnvRecord> listEnvRecordByLine(String productionLine) {
        QueryWrapper<ProdEnvRecord> qw = new QueryWrapper<>();
        qw.eq("production_line", productionLine);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("collect_time");
        return prodEnvRecordMapper.selectList(qw);
    }

    // 按异常状态查询
    public List<ProdEnvRecord> listEnvRecordByAbnormal(int isAbnormal) {
        QueryWrapper<ProdEnvRecord> qw = new QueryWrapper<>();
        qw.eq("is_abnormal", isAbnormal);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("collect_time");
        return prodEnvRecordMapper.selectList(qw);
    }

    // 采集环境数据
    public int recordEnv(ProdEnvRecord envRecord) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        envRecord.setCollectTime(now);
        envRecord.setCreateTime(now);
        envRecord.setUpdateTime(now);
        return prodEnvRecordMapper.insert(envRecord);
    }

    // 更新环境记录
    public int updateEnvRecord(ProdEnvRecord envRecord) {
        envRecord.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return prodEnvRecordMapper.updateById(envRecord);
    }

    // ==================== t_quality_inspection ====================

    // 按检验编号查询
    public QualityInspection getByInspectionNo(String inspectionNo) {
        QueryWrapper<QualityInspection> qw = new QueryWrapper<>();
        qw.eq("inspection_no", inspectionNo);
        qw.eq("is_deleted", 0);
        return qualityInspectionMapper.selectOne(qw);
    }

    // 按业务类型和批次号查询质检记录
    public List<QualityInspection> listByBizBatch(int bizType, String bizBatchNo) {
        QueryWrapper<QualityInspection> qw = new QueryWrapper<>();
        qw.eq("biz_type", bizType);
        qw.eq("biz_batch_no", bizBatchNo);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("create_time");
        return qualityInspectionMapper.selectList(qw);
    }

    // 按业务类型查询质检列表
    public List<QualityInspection> listByBizType(int bizType) {
        QueryWrapper<QualityInspection> qw = new QueryWrapper<>();
        qw.eq("biz_type", bizType);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("create_time");
        return qualityInspectionMapper.selectList(qw);
    }

    // 条件列表查询
    public List<QualityInspection> listInspection(Integer bizType, String bizBatchNo,
                                                   Integer inspectionType, Integer inspectionResult) {
        QueryWrapper<QualityInspection> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
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

    // 新增质检记录
    public int createInspection(QualityInspection inspection) {
        if (inspection.getInspectionNo() == null || inspection.getInspectionNo().isBlank()) {
            inspection.setInspectionNo(generateInspectionNo());
        }
        if (inspection.getInspectionResult() == 0) inspection.setInspectionResult(3);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        inspection.setCreateTime(now);
        inspection.setUpdateTime(now);
        return qualityInspectionMapper.insert(inspection);
    }

    // 更新质检记录
    public int updateInspection(QualityInspection inspection) {
        inspection.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return qualityInspectionMapper.updateById(inspection);
    }

    // ==================== 质检结果联动 ====================

    // 录入质检结果并联动更新生产批次
    public void recordQualityCheckForProd(String prodBatchNo, int checkResult) {
        ProdBatch prodBatch = getByProdBatchNo(prodBatchNo);
        if (prodBatch != null) {
            prodBatch.setCheckResult(checkResult);
            if (checkResult == 1) {
                prodBatch.setBatchStatus(2);
            }
            updateProdBatch(prodBatch);
        }
    }

    // 录入质检结果并联动更新加工批次
    public void recordQualityCheckForProcess(String processBatchNo, int checkResult) {
        ProcessBatch processBatch = getByProcessBatchNo(processBatchNo);
        if (processBatch != null) {
            if (checkResult == 1) {
                processBatch.setBatchStatus(2);
            }
            updateProcessBatch(processBatch);
        }
    }

    // ==================== 生产全链路追溯 ====================

    // 根据生产批次号追溯上游加工批次
    public ProcessBatch traceProcessChain(String prodBatchNo) {
        ProdBatch prodBatch = getByProdBatchNo(prodBatchNo);
        if (prodBatch == null || prodBatch.getProcessBatchNo() == null) {
            return null;
        }
        return getByProcessBatchNo(prodBatch.getProcessBatchNo());
    }
}

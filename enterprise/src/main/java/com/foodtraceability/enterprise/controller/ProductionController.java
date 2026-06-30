package com.foodtraceability.enterprise.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodtraceability.enterprise.entity.TechTemplate;
import com.foodtraceability.enterprise.entity.ProdBatch;
import com.foodtraceability.enterprise.entity.ProdMaterialInput;
import com.foodtraceability.enterprise.entity.ProdEnvRecord;
import com.foodtraceability.enterprise.entity.QualityInspection;
import com.foodtraceability.enterprise.service.ProductionService;

/**
 * 生产管理 Controller — 加工批次已合并到生产批次
 */
@RestController
@RequestMapping("/Production")
public class ProductionController {
    @Autowired
    private ProductionService productionService;

    // ==================== t_tech_template ====================


    @RequestMapping("/queryTemplate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public TechTemplate queryTemplate(String templateName) {
        return productionService.getByTemplateName(templateName);
    }

    @RequestMapping("/listTemplateByProduct")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public List<TechTemplate> listTemplateByProduct(String applicableProduct) {
        return productionService.listByApplicableProduct(applicableProduct);
    }

    @RequestMapping("/listTemplate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public List<TechTemplate> listTemplate(String applicableProduct, Integer templateStatus) {
        return productionService.listTemplate(applicableProduct, templateStatus);
    }

    @RequestMapping("/createTemplate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String createTemplate(TechTemplate template) {
        int num = productionService.createTemplate(template);
        return num == 1 ? "工艺模板创建成功" : "工艺模板创建失败";
    }

    @RequestMapping("/updateTemplate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String updateTemplate(TechTemplate template) {
        int num = productionService.updateTemplate(template);
        return num == 1 ? "工艺模板更新成功" : "工艺模板更新失败";
    }

    @RequestMapping("/deleteTemplate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String deleteTemplate(Long templateId) {
        int num = productionService.deleteTemplate(templateId);
        return num == 1 ? "工艺模板删除成功" : "工艺模板删除失败";
    }

    // ==================== t_prod_batch（含加工参数） ====================

    @RequestMapping("/queryProdBatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public ProdBatch queryProdBatch(String batchNo) {
        return productionService.getByProdBatchNo(batchNo);
    }

    /** 按原料批次号查询生产批次 */
    @RequestMapping("/listProdByRaw")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public List<ProdBatch> listProdByRaw(String rawBatchNo) {
        return productionService.listByRawBatchNo(rawBatchNo);
    }

    /** 条件列表查询 */
    @RequestMapping("/listProdBatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public List<ProdBatch> listProdBatch(String productName, String productionLine,
                                          Integer checkResult, Integer batchStatus,
                                          Integer codeStatus, String rawBatchNo) {
        return productionService.listProdBatch(productName, productionLine,
                checkResult, batchStatus, codeStatus, rawBatchNo);
    }

    /** 新增生产批次（含加工参数），返回完整对象以便前端获取自动生成的batchNo */
    @RequestMapping("/createProdBatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public ProdBatch createProdBatch(ProdBatch prodBatch) {
        productionService.createProdBatch(prodBatch);
        return prodBatch;
    }

    @RequestMapping("/updateProdBatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String updateProdBatch(ProdBatch prodBatch) {
        int num = productionService.updateProdBatch(prodBatch);
        return num == 1 ? "生产批次更新成功" : "生产批次更新失败";
    }

    @RequestMapping("/startProdBatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String startProdBatch(Long prodBatchId) {
        int num = productionService.startProdBatch(prodBatchId);
        return num == 1 ? "生产已开始" : "操作失败：批次不存在或状态不允许";
    }

    @RequestMapping("/completeProdBatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String completeProdBatch(Long prodBatchId) {
        int num = productionService.completeProdBatch(prodBatchId);
        return num == 1 ? "生产批次已完成" : "操作失败";
    }

    @RequestMapping("/bindCode")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String bindCode(Long prodBatchId) {
        int num = productionService.bindCodeComplete(prodBatchId);
        return num == 1 ? "溯源码绑定完成" : "绑码失败";
    }

    @RequestMapping("/deleteProdBatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String deleteProdBatch(Long prodBatchId) {
        int num = productionService.deleteProdBatch(prodBatchId);
        return num == 1 ? "生产批次删除成功" : "生产批次删除失败";
    }

    // ==================== t_prod_material_input ====================

    @RequestMapping("/queryMaterialInput")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public ProdMaterialInput queryMaterialInput(String rawBatchNo) {
        return productionService.getByRawBatchNo(rawBatchNo);
    }

    @RequestMapping("/listMaterialInput")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public List<ProdMaterialInput> listMaterialInput(String materialName) {
        return productionService.listMaterialInput(materialName);
    }

    @RequestMapping("/listMaterialInputByProdBatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public List<ProdMaterialInput> listMaterialInputByProdBatch(String prodBatchNo) {
        return productionService.listByProdBatchNo(prodBatchNo);
    }

    @RequestMapping("/recordMaterialInput")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String recordMaterialInput(ProdMaterialInput input) {
        int num = productionService.recordMaterialInput(input);
        return num == 1 ? "投料记录成功" : "投料记录失败";
    }

    @RequestMapping("/updateMaterialInput")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String updateMaterialInput(ProdMaterialInput input) {
        int num = productionService.updateMaterialInput(input);
        return num == 1 ? "投料记录更新成功" : "投料记录更新失败";
    }

    // ==================== t_quality_inspection ====================

    // listEnvAbnormal 保留供预警页面使用
    @RequestMapping("/listEnvAbnormal")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public List<ProdEnvRecord> listEnvAbnormal(Integer isAbnormal) {
        if (isAbnormal == null) isAbnormal = 1;
        return productionService.listEnvRecordByAbnormal(isAbnormal);
    }

    @RequestMapping("/queryInspection")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public QualityInspection queryInspection(String inspectionNo) {
        return productionService.getByInspectionNo(inspectionNo);
    }

    @RequestMapping("/listInspectionByBiz")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public List<QualityInspection> listInspectionByBiz(int bizType, String bizBatchNo) {
        return productionService.listByBizBatch(bizType, bizBatchNo);
    }

    @RequestMapping("/listInspectionByType")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public List<QualityInspection> listInspectionByType(int bizType) {
        return productionService.listByBizType(bizType);
    }

    @RequestMapping("/listInspection")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public List<QualityInspection> listInspection(Integer bizType, String bizBatchNo,
                                                   Integer inspectionType, Integer inspectionResult) {
        return productionService.listInspection(bizType, bizBatchNo, inspectionType, inspectionResult);
    }

    @RequestMapping("/createInspection")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String createInspection(QualityInspection inspection) {
        int num = productionService.createInspection(inspection);
        return num == 1 ? "质检记录创建成功，检验编号：" + inspection.getInspectionNo() : "质检记录创建失败";
    }

    @RequestMapping("/updateInspection")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String updateInspection(QualityInspection inspection) {
        int num = productionService.updateInspection(inspection);
        return num == 1 ? "质检记录更新成功" : "质检记录更新失败";
    }

    // ==================== 质检结果联动 ====================

    @RequestMapping("/qualityCheckProd")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String qualityCheckProd(String prodBatchNo, int checkResult) {
        productionService.recordQualityCheckForProd(prodBatchNo, checkResult);
        return "生产批次质检结果录入成功";
    }

    // ==================== 生产全链路追溯（生产批次 → 原料批次） ====================

    @RequestMapping("/traceProcessChain")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public ProductionService.ProductionChainTraceVO traceProcessChain(String prodBatchNo) {
        return productionService.traceProcessChain(prodBatchNo);
    }

    // ==================== 兼容旧加工批次端点（转发到生产批次） ====================

    /** @deprecated 加工批次已合并到生产批次，请使用 listProdByRaw */
    @RequestMapping("/listProcessByRaw")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER', 'REGULATOR')")
    public List<ProdBatch> listProcessByRaw(String rawBatchNo) {
        return productionService.listByRawBatchNo(rawBatchNo);
    }
}

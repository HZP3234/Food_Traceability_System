package com.foodtraceability.enterprise.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodtraceability.enterprise.entity.TechTemplate;
import com.foodtraceability.enterprise.entity.ProcessBatch;
import com.foodtraceability.enterprise.entity.ProdBatch;
import com.foodtraceability.enterprise.entity.ProdMaterialInput;
import com.foodtraceability.enterprise.entity.ProdEnvRecord;
import com.foodtraceability.enterprise.entity.QualityInspection;
import com.foodtraceability.enterprise.service.ProductionService;
//
@RestController
@RequestMapping("/Production")
public class ProductionController {
    @Autowired
    private ProductionService productionService;

    // ==================== t_tech_template ====================

    // 按模板名称查询
    @RequestMapping("/queryTemplate")
    public TechTemplate queryTemplate(String templateName) {
        return productionService.getByTemplateName(templateName);
    }

    // 按适用产品查询模板列表
    @RequestMapping("/listTemplateByProduct")
    public List<TechTemplate> listTemplateByProduct(String applicableProduct) {
        return productionService.listByApplicableProduct(applicableProduct);
    }

    // 条件列表查询
    @RequestMapping("/listTemplate")
    public List<TechTemplate> listTemplate(String applicableProduct, Integer templateStatus) {
        return productionService.listTemplate(applicableProduct, templateStatus);
    }

    // 新增工艺模板
    @RequestMapping("/createTemplate")
    public String createTemplate(TechTemplate template) {
        int num = productionService.createTemplate(template);
        if (num == 1) {
            return "工艺模板创建成功";
        } else {
            return "工艺模板创建失败";
        }
    }

    // 更新工艺模板
    @RequestMapping("/updateTemplate")
    public String updateTemplate(TechTemplate template) {
        int num = productionService.updateTemplate(template);
        if (num == 1) {
            return "工艺模板更新成功";
        } else {
            return "工艺模板更新失败";
        }
    }

    // 软删除工艺模板
    @RequestMapping("/deleteTemplate")
    public String deleteTemplate(Long templateId) {
        int num = productionService.deleteTemplate(templateId);
        if (num == 1) {
            return "工艺模板删除成功";
        } else {
            return "工艺模板删除失败";
        }
    }

    // ==================== t_process_batch ====================

    // 按加工批次号查询
    @RequestMapping("/queryProcessBatch")
    public ProcessBatch queryProcessBatch(String batchNo) {
        return productionService.getByProcessBatchNo(batchNo);
    }

    // 按原料批次号查询加工批次
    @RequestMapping("/listProcessByRaw")
    public List<ProcessBatch> listProcessByRaw(String rawBatchNo) {
        return productionService.listByRawBatchNo(rawBatchNo);
    }

    // 条件列表查询
    @RequestMapping("/listProcessBatch")
    public List<ProcessBatch> listProcessBatch(String productName, String productionLine,
                                                Integer batchStatus, Integer shift) {
        return productionService.listProcessBatch(productName, productionLine, batchStatus, shift);
    }

    // 新增加工批次
    @RequestMapping("/createProcessBatch")
    public String createProcessBatch(ProcessBatch processBatch) {
        int num = productionService.createProcessBatch(processBatch);
        if (num == 1) {
            return "加工批次创建成功，批次号：" + processBatch.getBatchNo();
        } else {
            return "加工批次创建失败";
        }
    }

    // 更新加工批次
    @RequestMapping("/updateProcessBatch")
    public String updateProcessBatch(ProcessBatch processBatch) {
        int num = productionService.updateProcessBatch(processBatch);
        if (num == 1) {
            return "加工批次更新成功";
        } else {
            return "加工批次更新失败";
        }
    }

    // 完成加工批次
    @RequestMapping("/completeProcessBatch")
    public String completeProcessBatch(Long processBatchId) {
        int num = productionService.completeProcessBatch(processBatchId);
        if (num == 1) {
            return "加工批次已完成";
        } else {
            return "操作失败";
        }
    }

    // 软删除加工批次
    @RequestMapping("/deleteProcessBatch")
    public String deleteProcessBatch(Long processBatchId) {
        int num = productionService.deleteProcessBatch(processBatchId);
        if (num == 1) {
            return "加工批次删除成功";
        } else {
            return "加工批次删除失败";
        }
    }

    // ==================== t_prod_batch ====================

    // 按生产批次号查询
    @RequestMapping("/queryProdBatch")
    public ProdBatch queryProdBatch(String batchNo) {
        return productionService.getByProdBatchNo(batchNo);
    }

    // 按加工批次号查询生产批次
    @RequestMapping("/listProdByProcess")
    public List<ProdBatch> listProdByProcess(String processBatchNo) {
        return productionService.listByProcessBatchNo(processBatchNo);
    }

    // 条件列表查询
    @RequestMapping("/listProdBatch")
    public List<ProdBatch> listProdBatch(String productName, String productionLine,
                                          Integer checkResult, Integer batchStatus,
                                          Integer codeStatus) {
        return productionService.listProdBatch(productName, productionLine,
                checkResult, batchStatus, codeStatus);
    }

    // 新增生产批次
    @RequestMapping("/createProdBatch")
    public String createProdBatch(ProdBatch prodBatch) {
        int num = productionService.createProdBatch(prodBatch);
        if (num == 1) {
            return "生产批次创建成功，批次号：" + prodBatch.getBatchNo();
        } else {
            return "生产批次创建失败";
        }
    }

    // 更新生产批次
    @RequestMapping("/updateProdBatch")
    public String updateProdBatch(ProdBatch prodBatch) {
        int num = productionService.updateProdBatch(prodBatch);
        if (num == 1) {
            return "生产批次更新成功";
        } else {
            return "生产批次更新失败";
        }
    }

    // 完成生产批次
    @RequestMapping("/completeProdBatch")
    public String completeProdBatch(Long prodBatchId) {
        int num = productionService.completeProdBatch(prodBatchId);
        if (num == 1) {
            return "生产批次已完成";
        } else {
            return "操作失败";
        }
    }

    // 绑码完成
    @RequestMapping("/bindCode")
    public String bindCode(Long prodBatchId) {
        int num = productionService.bindCodeComplete(prodBatchId);
        if (num == 1) {
            return "溯源码绑定完成";
        } else {
            return "绑码失败";
        }
    }

    // 软删除生产批次
    @RequestMapping("/deleteProdBatch")
    public String deleteProdBatch(Long prodBatchId) {
        int num = productionService.deleteProdBatch(prodBatchId);
        if (num == 1) {
            return "生产批次删除成功";
        } else {
            return "生产批次删除失败";
        }
    }

    // ==================== t_prod_material_input ====================

    // 按原料批次号查询投料记录
    @RequestMapping("/queryMaterialInput")
    public ProdMaterialInput queryMaterialInput(String rawBatchNo) {
        return productionService.getByRawBatchNo(rawBatchNo);
    }

    // 投料记录列表
    @RequestMapping("/listMaterialInput")
    public List<ProdMaterialInput> listMaterialInput(String materialName) {
        return productionService.listMaterialInput(materialName);
    }

    // 记录投料
    @RequestMapping("/recordMaterialInput")
    public String recordMaterialInput(ProdMaterialInput input) {
        int num = productionService.recordMaterialInput(input);
        if (num == 1) {
            return "投料记录成功";
        } else {
            return "投料记录失败";
        }
    }

    // 更新投料记录
    @RequestMapping("/updateMaterialInput")
    public String updateMaterialInput(ProdMaterialInput input) {
        int num = productionService.updateMaterialInput(input);
        if (num == 1) {
            return "投料记录更新成功";
        } else {
            return "投料记录更新失败";
        }
    }

    // ==================== t_prod_env_record ====================

    // 按生产线查询环境记录
    @RequestMapping("/listEnvRecord")
    public List<ProdEnvRecord> listEnvRecord(String productionLine) {
        return productionService.listEnvRecordByLine(productionLine);
    }

    // 按异常状态查询
    @RequestMapping("/listEnvAbnormal")
    public List<ProdEnvRecord> listEnvAbnormal(Integer isAbnormal) {
        if (isAbnormal == null) isAbnormal = 1;
        return productionService.listEnvRecordByAbnormal(isAbnormal);
    }

    // 采集环境数据
    @RequestMapping("/recordEnv")
    public String recordEnv(ProdEnvRecord envRecord) {
        int num = productionService.recordEnv(envRecord);
        if (num == 1) {
            return "环境数据采集成功";
        } else {
            return "环境数据采集失败";
        }
    }

    // 更新环境记录
    @RequestMapping("/updateEnvRecord")
    public String updateEnvRecord(ProdEnvRecord envRecord) {
        int num = productionService.updateEnvRecord(envRecord);
        if (num == 1) {
            return "环境记录更新成功";
        } else {
            return "环境记录更新失败";
        }
    }

    // ==================== t_quality_inspection ====================

    // 按检验编号查询
    @RequestMapping("/queryInspection")
    public QualityInspection queryInspection(String inspectionNo) {
        return productionService.getByInspectionNo(inspectionNo);
    }

    // 按业务类型和批次号查询质检记录
    @RequestMapping("/listInspectionByBiz")
    public List<QualityInspection> listInspectionByBiz(int bizType, String bizBatchNo) {
        return productionService.listByBizBatch(bizType, bizBatchNo);
    }

    // 按业务类型查询质检列表
    @RequestMapping("/listInspectionByType")
    public List<QualityInspection> listInspectionByType(int bizType) {
        return productionService.listByBizType(bizType);
    }

    // 条件列表查询
    @RequestMapping("/listInspection")
    public List<QualityInspection> listInspection(Integer bizType, String bizBatchNo,
                                                   Integer inspectionType, Integer inspectionResult) {
        return productionService.listInspection(bizType, bizBatchNo, inspectionType, inspectionResult);
    }

    // 新增质检记录
    @RequestMapping("/createInspection")
    public String createInspection(QualityInspection inspection) {
        int num = productionService.createInspection(inspection);
        if (num == 1) {
            return "质检记录创建成功，检验编号：" + inspection.getInspectionNo();
        } else {
            return "质检记录创建失败";
        }
    }

    // 更新质检记录
    @RequestMapping("/updateInspection")
    public String updateInspection(QualityInspection inspection) {
        int num = productionService.updateInspection(inspection);
        if (num == 1) {
            return "质检记录更新成功";
        } else {
            return "质检记录更新失败";
        }
    }

    // ==================== 质检结果联动 ====================

    // 录入生产批次质检结果
    @RequestMapping("/qualityCheckProd")
    public String qualityCheckProd(String prodBatchNo, int checkResult) {
        productionService.recordQualityCheckForProd(prodBatchNo, checkResult);
        return "生产批次质检结果录入成功";
    }

    // 录入加工批次质检结果
    @RequestMapping("/qualityCheckProcess")
    public String qualityCheckProcess(String processBatchNo, int checkResult) {
        productionService.recordQualityCheckForProcess(processBatchNo, checkResult);
        return "加工批次质检结果录入成功";
    }

    // ==================== 生产全链路追溯 ====================

    // 根据生产批次号追溯全链路：生产批次 → 加工批次 → 原料批次
    @RequestMapping("/traceProcessChain")
    public ProductionService.ProductionChainTraceVO traceProcessChain(String prodBatchNo) {
        return productionService.traceProcessChain(prodBatchNo);
    }
}

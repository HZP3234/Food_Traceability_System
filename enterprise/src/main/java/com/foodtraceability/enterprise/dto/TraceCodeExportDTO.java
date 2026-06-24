package com.foodtraceability.enterprise.dto;

import java.util.List;

/**
 * 溯源码标签导出请求 DTO
 * <p>
 * 用于导出溯源码二维码/条形码标签文件。
 *
 * @author GuangYao Liu
 * @since 2026-06-23
 */
public class TraceCodeExportDTO {

    /** 需要导出的溯源码值列表 */
    private List<String> traceCodeList;

    /** 标签模板ID：1-标准标签 2-简化标签 */
    private Integer templateId;

    /**
     * 导出格式：
     * PDF  - PDF 文档
     * PNG  - 图片压缩包
     * ZPL  - 斑马打印机指令
     */
    private String exportFormat;

    /** 操作人 */
    private String operator;

    // ==================== Getter / Setter ====================

    public List<String> getTraceCodeList() { return traceCodeList; }
    public void setTraceCodeList(List<String> traceCodeList) { this.traceCodeList = traceCodeList; }

    public Integer getTemplateId() { return templateId; }
    public void setTemplateId(Integer templateId) { this.templateId = templateId; }

    public String getExportFormat() { return exportFormat; }
    public void setExportFormat(String exportFormat) { this.exportFormat = exportFormat; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
}

package com.foodtraceability.regulation.dto;

import lombok.Data;

/**
 * 投诉处理操作请求 DTO
 */
@Data
public class ComplaintHandleDTO {

    /** 投诉记录ID */
    private Long complaintRecordId;

    /** 操作动作: ACCEPT / INVESTIGATE / RESPOND / CLOSE / REJECT */
    private String action;

    /** 操作备注/处理说明 */
    private String remark;

    /** 处理结论（CLOSE 时必填） */
    private String handlingConclusion;

    /** 处理凭证URL列表（逗号分隔） */
    private String handlingProofUrls;

    /** 优先级调整（受理时可设置） */
    private Integer priority;
}

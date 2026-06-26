package com.foodtraceability.regulation.dto;

import lombok.Data;

/**
 * 投诉分页查询请求 DTO
 */
@Data
public class ComplaintPageDTO {

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 10 */
    private Integer size = 10;

    /** 投诉编号（模糊搜索） */
    private String complaintNo;

    /** 被投诉企业名称（模糊搜索） */
    private String enterpriseName;

    /** 投诉人姓名（模糊搜索） */
    private String complainantName;

    /** 投诉类型: 1-产品质量 2-食品安全 3-包装问题 4-虚假宣传 5-其他 */
    private Integer complaintType;

    /** 状态: 1-已提交 2-处理中 3-已处理 4-已驳回 */
    private Integer status;

    /** 优先级: 1-普通 2-紧急 3-重大 */
    private Integer priority;

    /** 提交时间起 (yyyy-MM-dd) */
    private String submitTimeStart;

    /** 提交时间止 (yyyy-MM-dd) */
    private String submitTimeEnd;
}

package com.foodtraceability.customers.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ComplaintSubmitDTO {

    /** 关联溯源码 */
    private String traceCode;

    /** 关联批次号 */
    @NotBlank(message = "产品批次号不能为空")
    private String batchNumber;

    /** 被投诉企业ID */
    private String enterpriseUuid;

    /** 被投诉企业名称 */
    @NotBlank(message = "企业名称不能为空")
    private String enterpriseName;

    /** 投诉人UUID（必须先登录获取） */
    @NotBlank(message = "请先登录后再提交投诉")
    private String consumerUuid;

    /** 投诉人姓名 */
    @NotBlank(message = "投诉人姓名不能为空")
    private String complainantName;

    /** 联系电话 */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 是否匿名: 0-否 1-是 */
    private Integer isAnonymous;

    /** 投诉类型: 1-产品质量 2-食品安全 3-包装问题 4-虚假宣传 5-其他 */
    @NotNull(message = "投诉类型不能为空")
    @Min(value = 1, message = "投诉类型必须在1-5之间")
    @Max(value = 5, message = "投诉类型必须在1-5之间")
    private Integer complaintType;

    /** 投诉描述 */
    @NotBlank(message = "投诉内容不能为空")
    private String description;

    /** 照片URL列表 */
    private String photoUrls;
}

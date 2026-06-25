package com.foodtraceability.customers.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ComplaintSubmitDTO {

    @NotBlank(message = "产品批次号不能为空")
    private String productBatchNo;

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    @NotBlank(message = "消费者姓名不能为空")
    private String consumerName;

    @NotBlank(message = "消费者电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String consumerPhone;

    private Long consumerId;

    @NotNull(message = "投诉类型不能为空")
    @Min(value = 1, message = "投诉类型必须在1-5之间")
    @Max(value = 5, message = "投诉类型必须在1-5之间")
    private Integer complaintType;

    @NotBlank(message = "投诉标题不能为空")
    @Size(max = 256, message = "投诉标题不能超过256字")
    private String complaintTitle;

    @NotBlank(message = "投诉内容不能为空")
    private String complaintContent;

    private String imageUrls;
}

package com.foodtraceability.customers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComplaintFeedbackDTO {

    @NotNull(message = "投诉ID不能为空")
    private Long complaintId;

    @NotBlank(message = "反馈内容不能为空")
    private String feedbackContent;

    @NotBlank(message = "反馈人不能为空")
    private String feedbackBy;
}

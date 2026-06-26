package com.foodtraceability.regulation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 投诉统计概览
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintStats {

    /** 总投诉数 */
    @JsonProperty("total")
    private Long totalCount;

    /** 待受理数 (status=1) */
    @JsonProperty("pending")
    private Long pendingCount;

    /** 处理中数 (status=2) */
    @JsonProperty("processing")
    private Long processingCount;

    /** 已处理数 (status=3) */
    @JsonProperty("resolved")
    private Long resolvedCount;

    /** 已驳回数 (status=4) */
    @JsonProperty("rejected")
    private Long rejectedCount;

    /** 紧急+重大数 */
    @JsonProperty("urgent")
    private Long urgentCount;
}

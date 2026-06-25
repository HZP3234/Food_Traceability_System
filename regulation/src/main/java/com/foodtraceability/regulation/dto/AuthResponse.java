package com.foodtraceability.regulation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /** JWT token */
    private String token;

    /** 用户名 */
    private String username;

    /** 角色类型 */
    private String roleType;

    /** 真实姓名 */
    private String realName;

    /** 关联企业UUID */
    private String enterpriseUuid;

    /** 企业类型: 1-供应商 2-加工商 3-物流商 4-零售商 (仅企业用户有值) */
    private Integer enterpriseType;

    /** 是否已提交过企业资质 */
    private Boolean hasQualification;

    /** 企业名称（仅企业用户有值） */
    private String enterpriseName;
}

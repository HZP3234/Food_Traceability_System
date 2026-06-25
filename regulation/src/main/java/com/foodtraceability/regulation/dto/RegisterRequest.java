package com.foodtraceability.regulation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册请求 — 企业用户注册时一并传入企业信息，一站式创建
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "角色类型不能为空")
    private String roleType;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    // ==================== 企业用户专用字段 ====================

    /** 企业类型: 1-供应商 2-生产商 3-物流商 4-销售商 */
    private Integer enterpriseType;

    /** 企业名称 */
    private String enterpriseName;

    /** 统一社会信用代码 */
    private String certNo;

    /** 联系电话 */
    private String contactPhone;

    /** 联系人 */
    private String contactPerson;

    /** 注册地址 */
    private String address;

    // ==================== 可选字段 ====================

    private String phone;
    private String email;
}

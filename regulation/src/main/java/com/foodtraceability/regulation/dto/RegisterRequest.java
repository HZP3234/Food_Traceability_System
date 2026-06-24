package com.foodtraceability.regulation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册请求
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "角色类型不能为空")
    private String roleType;

    /** 企业用户时必填 */
    private String enterpriseUuid;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private String phone;

    private String email;
}

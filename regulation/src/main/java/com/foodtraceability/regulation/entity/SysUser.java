package com.foodtraceability.regulation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户 — 对应 t_sys_user 表
 */
@Data
@TableName("t_sys_user")
public class SysUser {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /** 用户UUID（全局唯一） */
    private String userUuid;

    /** 登录用户名 */
    private String username;

    /** bcrypt 加密密码 */
    private String passwordHash;

    /** 角色类型: ADMIN / REGULATOR / ENTERPRISE */
    private String roleType;

    /** 关联企业UUID（企业用户时必填） */
    private String enterpriseUuid;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 状态: 1-启用 0-禁用 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableLogic
    private Integer isDeleted;
}

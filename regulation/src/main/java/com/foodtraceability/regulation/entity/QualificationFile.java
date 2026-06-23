package com.foodtraceability.regulation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 企业资质文件 — 对应 t_qualification 表 (需求 3.2.9-b)
 */
@Data
@TableName("t_qualification")
public class QualificationFile {

    @TableId(value = "qualification_id", type = IdType.AUTO)
    private Long qualificationId;

    /** 资质UUID */
    private String qualificationUuid;

    /** 关联企业UUID */
    private String enterpriseUuid;

    /** 资质类型: 1-营业执照 2-生产许可证 3-经营许可证 4-其他 */
    private Integer qualificationType;

    /** 资质编号 */
    private String qualificationNo;

    /** 颁发机构 */
    private String issueAuthority;

    /** 有效期起 */
    private LocalDate validFrom;

    /** 有效期止 */
    private LocalDate validTo;

    /** 文件存储路径 */
    private String filePath;

    /** 文件Hash (SHA-256) */
    private String fileHash;

    /** 资质状态: 1-有效 2-即将过期 3-已失效 */
    private Integer qualificationState;

    /** 审核状态: 0-待审核 1-审核通过 2-审核不通过 */
    private Integer auditState;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String createBy;
    private String updateBy;

    @TableLogic
    private Integer isDeleted;
}

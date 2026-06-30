-- =============================================
-- 溯源码管理模块 — 数据库建表脚本
-- 模块归属：enterprise（企业端）
-- 对应概要设计说明书 表格21 溯源码管理模块数据结构
-- 作者：GuangYao Liu
-- 日期：2026-06-23
-- =============================================

-- 溯源码主表
CREATE TABLE IF NOT EXISTS `t_trace_code` (
    `trace_code_id`      INT(11)       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `trace_code`          VARCHAR(64)   NOT NULL                 COMMENT '溯源码值，全局唯一',
    `code_type`           INT(11)       DEFAULT 1                COMMENT '码类型：1-单品码 2-批次码 3-箱码 4-托码',
    `package_level`       INT(11)       DEFAULT 1                COMMENT '包装层级：1-最小销售单元 2-外箱 3-托盘',
    `product_id`          VARCHAR(64)   DEFAULT NULL             COMMENT '产品ID',
    `product_name`        VARCHAR(255)  DEFAULT NULL             COMMENT '产品名称（冗余）',
    `enterprise_id`       VARCHAR(64)   DEFAULT NULL             COMMENT '责任企业ID',
    `enterprise_name`     VARCHAR(255)  DEFAULT NULL             COMMENT '责任企业名称（冗余）',
    `trace_code_status`   INT(11)       DEFAULT 0                COMMENT '状态：0-未绑定 1-已绑定 2-已激活 3-已禁用 4-已作废 5-已过期',
    `batch_no`            VARCHAR(64)   DEFAULT NULL             COMMENT '关联生产批次号',
    `content_hash`        VARCHAR(128)  DEFAULT NULL             COMMENT 'SHA-256内容哈希',
    `proof_id`            VARCHAR(128)  DEFAULT NULL             COMMENT '区块链存证记录编号',
    `tx_hash`             VARCHAR(256)  DEFAULT NULL             COMMENT '区块链交易哈希',
    `generate_count`      INT(11)       DEFAULT 1                COMMENT '批量生成数量',
    `generate_batch_no`   VARCHAR(64)   DEFAULT NULL             COMMENT '批量生成批次号',
    `disable_reason`      VARCHAR(500)  DEFAULT NULL             COMMENT '禁用原因',
    `void_reason`         VARCHAR(500)  DEFAULT NULL             COMMENT '作废原因',
    `expire_time`         VARCHAR(32)   DEFAULT NULL             COMMENT '有效期截止时间',
    `is_on_chain`         INT(11)       DEFAULT 0                COMMENT '是否上链：0-否 1-是',
    `generate_time`       VARCHAR(32)   DEFAULT NULL             COMMENT '生成时间',
    `enable_time`         VARCHAR(32)   DEFAULT NULL             COMMENT '启用时间',
    `operator`            VARCHAR(64)   DEFAULT NULL             COMMENT '操作人',
    `create_by`           VARCHAR(64)   DEFAULT NULL             COMMENT '创建人',
    `create_time`         VARCHAR(32)   DEFAULT NULL             COMMENT '创建时间',
    `update_by`           VARCHAR(64)   DEFAULT NULL             COMMENT '更新人',
    `update_time`         VARCHAR(32)   DEFAULT NULL             COMMENT '更新时间',
    `is_deleted`          INT(11)       DEFAULT 0                COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (`trace_code_id`),
    UNIQUE KEY `uk_trace_code` (`trace_code`),
    KEY `idx_batch_no` (`batch_no`),
    KEY `idx_enterprise_id` (`enterprise_id`),
    KEY `idx_generate_batch_no` (`generate_batch_no`),
    KEY `idx_status` (`trace_code_status`),
    KEY `idx_code_type` (`code_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='溯源码主表';

-- 溯源码绑定关系表
CREATE TABLE IF NOT EXISTS `t_trace_code_bind` (
    `bind_id`      INT(11)      NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `trace_code`   VARCHAR(64)  NOT NULL                 COMMENT '溯源码值（外键关联t_trace_code）',
    `biz_type`     VARCHAR(32)  NOT NULL                 COMMENT '业务类型：RAW_BATCH/PROD_BATCH/PROCESS_BATCH/INSPECTION/LOGISTICS/SALES_TERMINAL',
    `biz_id`       VARCHAR(64)  DEFAULT NULL             COMMENT '业务数据编号（对应各业务表主键）',
    `biz_no`       VARCHAR(64)  DEFAULT NULL             COMMENT '业务数据编码（如批次号、物流单号，冗余便于展示）',
    `create_time`  VARCHAR(32)  DEFAULT NULL             COMMENT '绑定时间',
    `operator`     VARCHAR(64)  DEFAULT NULL             COMMENT '绑定操作人',
    `is_deleted`   INT(11)      DEFAULT 0                COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (`bind_id`),
    KEY `idx_trace_code` (`trace_code`),
    KEY `idx_biz_type` (`biz_type`),
    KEY `idx_biz_no` (`biz_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='溯源码绑定关系表';

-- =============================================
-- 销售订单管理模块 — 数据库建表脚本
-- 模块归属：enterprise（企业端）
-- 表1: t_sales_order — 销售订单主表（生产商创建）
-- 表2: t_sales_order_detail — 销售订单详情表（销售商上传）
-- 日期：2026-06-26
-- =============================================

CREATE TABLE IF NOT EXISTS `t_sales_order` (
    `sales_order_id`        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `sales_order_code`      VARCHAR(32)  NOT NULL                 COMMENT '销售订单编码 SO+日期+4位序号',
    `product_name`          VARCHAR(255) DEFAULT NULL             COMMENT '产品名称',
    `prod_batch_no`         VARCHAR(64)  DEFAULT NULL             COMMENT '关联生产批次号',
    `buyer_enterprise_id`   VARCHAR(64)  DEFAULT NULL             COMMENT '买方(销售商)企业ID',
    `buyer_enterprise_name` VARCHAR(255) DEFAULT NULL             COMMENT '买方(销售商)企业名称',
    `seller_enterprise_name` VARCHAR(255) DEFAULT NULL            COMMENT '卖方(生产商)企业名称',
    `order_quantity`        INT          DEFAULT 0                COMMENT '订单数量',
    `unit_price`            DECIMAL(10,2) DEFAULT 0.00            COMMENT '单价',
    `total_amount`          DECIMAL(12,2) DEFAULT 0.00            COMMENT '总金额',
    `order_date`            VARCHAR(32)  DEFAULT NULL             COMMENT '订单日期',
    `order_status`          INT          DEFAULT 1                COMMENT '订单状态：1-待补充 2-已补充 3-已完成',
    `detail_id`             VARCHAR(32)  DEFAULT NULL             COMMENT '关联详情ID',
    `detail_status`         INT          DEFAULT 0                COMMENT '详情状态：0-未上传 1-已上传',
    `remark`                VARCHAR(500) DEFAULT NULL             COMMENT '备注',
    `create_by`             VARCHAR(64)  DEFAULT NULL             COMMENT '创建人',
    `create_time`           VARCHAR(32)  DEFAULT NULL             COMMENT '创建时间',
    `update_by`             VARCHAR(64)  DEFAULT NULL             COMMENT '更新人',
    `update_time`           VARCHAR(32)  DEFAULT NULL             COMMENT '更新时间',
    `is_deleted`            INT          DEFAULT 0                COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (`sales_order_id`),
    UNIQUE KEY `uk_sales_order_code` (`sales_order_code`),
    KEY `idx_buyer_name` (`buyer_enterprise_name`),
    KEY `idx_order_status` (`order_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单主表';

CREATE TABLE IF NOT EXISTS `t_sales_order_detail` (
    `detail_id`             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `detail_code`           VARCHAR(32)  NOT NULL                 COMMENT '详情编码 SD+日期+4位序号',
    `sales_order_id`        BIGINT       DEFAULT NULL             COMMENT '关联销售订单ID',
    `sales_order_code`      VARCHAR(32)  DEFAULT NULL             COMMENT '关联销售订单编码',
    `temperature`           VARCHAR(32)  DEFAULT NULL             COMMENT '入库温度',
    `humidity`              VARCHAR(32)  DEFAULT NULL             COMMENT '入库湿度',
    `storage_method`        INT          DEFAULT 0                COMMENT '储存方式：0-常温 1-冷藏 2-冷冻',
    `light_condition`       INT          DEFAULT 0                COMMENT '光照条件：0-避光 1-散光 2-光照',
    `shelf_life`            VARCHAR(32)  DEFAULT NULL             COMMENT '上架保质期',
    `location_code`         VARCHAR(64)  DEFAULT NULL             COMMENT '仓库/柜位编号',
    `inbound_time`          VARCHAR(32)  DEFAULT NULL             COMMENT '入库时间',
    `actual_quantity`       INT          DEFAULT 0                COMMENT '实际销售数量',
    `sales_date`            VARCHAR(32)  DEFAULT NULL             COMMENT '销售日期',
    `detail_status`         INT          DEFAULT 1                COMMENT '详情状态：1-已提交 2-已审核',
    `data_hash`             VARCHAR(128) DEFAULT NULL             COMMENT '数据哈希',
    `chain_hash`            VARCHAR(256) DEFAULT NULL             COMMENT '区块链哈希',
    `operator`              VARCHAR(64)  DEFAULT NULL             COMMENT '操作员',
    `remark`                VARCHAR(500) DEFAULT NULL             COMMENT '备注',
    `create_by`             VARCHAR(64)  DEFAULT NULL             COMMENT '创建人',
    `create_time`           VARCHAR(32)  DEFAULT NULL             COMMENT '创建时间',
    `update_by`             VARCHAR(64)  DEFAULT NULL             COMMENT '更新人',
    `update_time`           VARCHAR(32)  DEFAULT NULL             COMMENT '更新时间',
    `is_deleted`            INT          DEFAULT 0                COMMENT '逻辑删除：0-正常 1-已删除',
    PRIMARY KEY (`detail_id`),
    UNIQUE KEY `uk_detail_code` (`detail_code`),
    KEY `idx_sales_order_id` (`sales_order_id`),
    KEY `idx_sales_order_code` (`sales_order_code`),
    KEY `idx_detail_status` (`detail_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单详情表';

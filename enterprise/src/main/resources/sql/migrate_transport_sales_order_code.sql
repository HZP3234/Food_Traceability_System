-- =============================================
-- 迁移脚本：运输订单表添加销售编码字段
-- 表: t_cc_transport
-- 日期：2026-06-29
-- =============================================

ALTER TABLE t_cc_transport
    ADD COLUMN sales_order_code VARCHAR(32) DEFAULT NULL COMMENT '关联销售编码' AFTER prod_batch_no;

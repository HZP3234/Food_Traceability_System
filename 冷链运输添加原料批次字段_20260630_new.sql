-- =============================================
-- 迁移脚本：冷链运输订单表添加原料批次号字段
-- 表: t_cc_transport
-- 用途：支持原料商→加工商的原料冷链运输，
--       区分于加工商→销售商的成品运输(prod_batch_no)
-- 日期：2026-06-30
-- =============================================

ALTER TABLE t_cc_transport
    ADD COLUMN raw_batch_no VARCHAR(64) DEFAULT NULL COMMENT '关联原料批次号' AFTER prod_batch_no;

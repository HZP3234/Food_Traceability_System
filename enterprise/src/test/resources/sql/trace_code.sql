-- =============================================
-- 溯源码管理模块 — H2 测试数据库建表脚本
-- H2 MySQL 兼容模式
-- =============================================

-- 溯源码主表
CREATE TABLE IF NOT EXISTS t_trace_code (
    trace_code_id      INT          NOT NULL AUTO_INCREMENT,
    trace_code_uuid    VARCHAR(64)  NOT NULL,
    trace_code          VARCHAR(64)  NOT NULL,
    code_type           INT          DEFAULT 1,
    package_level       INT          DEFAULT 1,
    product_id          VARCHAR(64),
    product_name        VARCHAR(255),
    enterprise_uuid     VARCHAR(40)  NOT NULL,
    enterprise_name     VARCHAR(255),
    trace_code_status   INT          DEFAULT 0,
    batch_no            VARCHAR(64),
    content_hash        VARCHAR(128),
    proof_id            VARCHAR(128),
    tx_hash             VARCHAR(256),
    generate_count      INT          DEFAULT 1,
    disable_reason      VARCHAR(500),
    void_reason         VARCHAR(500),
    expire_time         TIMESTAMP,
    create_by           VARCHAR(64),
    create_time         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by           VARCHAR(64),
    update_time         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted          INT          DEFAULT 0,
    PRIMARY KEY (trace_code_id),
    UNIQUE KEY uk_trace_code_uuid (trace_code_uuid),
    UNIQUE KEY uk_trace_code (trace_code)
);

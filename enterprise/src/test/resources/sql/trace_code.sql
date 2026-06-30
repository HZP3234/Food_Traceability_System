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
    enterprise_id       VARCHAR(64),
    enterprise_name     VARCHAR(255),
    trace_code_status   INT          DEFAULT 0,
    batch_no            VARCHAR(64),
    content_hash        VARCHAR(128),
    proof_id            VARCHAR(128),
    tx_hash             VARCHAR(256),
    generate_count      INT          DEFAULT 1,
    generate_batch_no   VARCHAR(64),
    disable_reason      VARCHAR(500),
    void_reason         VARCHAR(500),
    expire_time         VARCHAR(32),
    is_on_chain         INT          DEFAULT 0,
    generate_time       VARCHAR(32),
    enable_time         VARCHAR(32),
    operator            VARCHAR(64),
    create_by           VARCHAR(64),
    create_time         VARCHAR(32),
    update_by           VARCHAR(64),
    update_time         VARCHAR(32),
    is_deleted          INT          DEFAULT 0,
    PRIMARY KEY (trace_code_id),
    UNIQUE KEY uk_trace_code_uuid (trace_code_uuid),
    UNIQUE KEY uk_trace_code (trace_code)
);

-- 溯源码绑定关系表
CREATE TABLE IF NOT EXISTS t_trace_code_bind (
    bind_id      INT          NOT NULL AUTO_INCREMENT,
    trace_code   VARCHAR(64)  NOT NULL,
    biz_type     VARCHAR(32)  NOT NULL,
    biz_id       VARCHAR(64),
    biz_no       VARCHAR(64),
    create_time  VARCHAR(32),
    operator     VARCHAR(64),
    is_deleted   INT          DEFAULT 0,
    PRIMARY KEY (bind_id)
);

USE food_traceability;

CREATE TABLE IF NOT EXISTS product_traceability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_batch_no VARCHAR(64) NOT NULL COMMENT '产品批次号',
    product_name VARCHAR(128) NOT NULL COMMENT '产品名称',
    product_spec VARCHAR(128) COMMENT '产品规格',
    manufacturer VARCHAR(256) COMMENT '生产厂家',
    origin VARCHAR(256) COMMENT '产地',
    production_date DATE COMMENT '生产日期',
    expiration_date DATE COMMENT '保质期至',
    qr_code_url VARCHAR(512) COMMENT '溯源码URL',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除 1-已删除',
    UNIQUE INDEX idx_product_batch_no (product_batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品溯源信息表';

CREATE TABLE IF NOT EXISTS traceability_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_batch_no VARCHAR(64) NOT NULL COMMENT '产品批次号',
    node_name VARCHAR(128) NOT NULL COMMENT '节点名称',
    node_description TEXT COMMENT '节点描述',
    node_time DATETIME COMMENT '节点时间',
    location VARCHAR(256) COMMENT '地点',
    operator VARCHAR(128) COMMENT '操作人/单位',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除 1-已删除',
    INDEX idx_product_batch_no (product_batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='溯源节点表';

CREATE TABLE IF NOT EXISTS scan_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_batch_no VARCHAR(64) NOT NULL COMMENT '产品批次号',
    scan_ip VARCHAR(64) COMMENT '扫码IP',
    scan_location VARCHAR(256) COMMENT '扫码定位',
    scan_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '扫码时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除 1-已删除',
    INDEX idx_product_batch_no (product_batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费者扫码记录表';

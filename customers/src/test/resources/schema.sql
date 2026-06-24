CREATE TABLE IF NOT EXISTS product_traceability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_batch_no VARCHAR(64) NOT NULL,
    product_name VARCHAR(128) NOT NULL,
    product_spec VARCHAR(128),
    manufacturer VARCHAR(256),
    origin VARCHAR(256),
    production_date DATE,
    expiration_date DATE,
    qr_code_url VARCHAR(512),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE INDEX idx_product_batch_no (product_batch_no)
);

CREATE TABLE IF NOT EXISTS traceability_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_batch_no VARCHAR(64) NOT NULL,
    node_name VARCHAR(128) NOT NULL,
    node_description TEXT,
    node_time TIMESTAMP,
    location VARCHAR(256),
    operator VARCHAR(128),
    sort_order INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_product_batch_no (product_batch_no)
);

CREATE TABLE IF NOT EXISTS scan_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_batch_no VARCHAR(64) NOT NULL,
    scan_ip VARCHAR(64),
    scan_location VARCHAR(256),
    scan_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_product_batch_no (product_batch_no)
);

CREATE TABLE IF NOT EXISTS t_complaint_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_no VARCHAR(32) NOT NULL,
    product_batch_no VARCHAR(64),
    product_name VARCHAR(128),
    consumer_name VARCHAR(64),
    consumer_phone VARCHAR(20),
    complaint_type TINYINT NOT NULL,
    complaint_title VARCHAR(256) NOT NULL,
    complaint_content TEXT NOT NULL,
    image_urls VARCHAR(1024),
    status TINYINT NOT NULL DEFAULT 0,
    feedback_content TEXT,
    feedback_time TIMESTAMP,
    feedback_by VARCHAR(64),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_complaint_no (complaint_no),
    INDEX idx_product_batch_no (product_batch_no),
    INDEX idx_consumer_phone (consumer_phone),
    INDEX idx_status (status)
);

CREATE TABLE IF NOT EXISTS consumer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) NOT NULL,
    nickname VARCHAR(64),
    avatar VARCHAR(256),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE INDEX idx_phone (phone)
);

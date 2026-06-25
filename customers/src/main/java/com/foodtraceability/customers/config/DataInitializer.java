package com.foodtraceability.customers.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Configuration
@Profile("test")
public class DataInitializer implements CommandLineRunner {

    private final DataSource dataSource;

    public DataInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS product_traceability (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "product_batch_no VARCHAR(64) NOT NULL," +
                "product_name VARCHAR(128) NOT NULL," +
                "product_spec VARCHAR(128)," +
                "manufacturer VARCHAR(256)," +
                "origin VARCHAR(256)," +
                "production_date DATE," +
                "expiration_date DATE," +
                "qr_code_url VARCHAR(512)," +
                "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "deleted TINYINT NOT NULL DEFAULT 0," +
                "UNIQUE INDEX idx_product_batch_no (product_batch_no))");

            stmt.execute("CREATE TABLE IF NOT EXISTS traceability_node (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "product_batch_no VARCHAR(64) NOT NULL," +
                "node_name VARCHAR(128) NOT NULL," +
                "node_description CLOB," +
                "node_time TIMESTAMP," +
                "location VARCHAR(256)," +
                "operator VARCHAR(128)," +
                "sort_order INT NOT NULL DEFAULT 0," +
                "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "deleted TINYINT NOT NULL DEFAULT 0," +
                "INDEX idx_node_batch_no (product_batch_no))");

            stmt.execute("CREATE TABLE IF NOT EXISTS scan_record (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "product_batch_no VARCHAR(64) NOT NULL," +
                "scan_ip VARCHAR(64)," +
                "scan_location VARCHAR(256)," +
                "scan_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "deleted TINYINT NOT NULL DEFAULT 0," +
                "INDEX idx_scan_batch_no (product_batch_no))");

            stmt.execute("DROP TABLE IF EXISTS t_complaint_record");
            stmt.execute("CREATE TABLE t_complaint_record (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "complaint_no VARCHAR(32) NOT NULL," +
                "product_batch_no VARCHAR(64)," +
                "product_name VARCHAR(128)," +
                "consumer_name VARCHAR(64)," +
                "consumer_phone VARCHAR(20)," +
                "consumer_id BIGINT," +
                "complaint_type TINYINT NOT NULL," +
                "complaint_title VARCHAR(256) NOT NULL," +
                "complaint_content TEXT NOT NULL," +
                "image_urls VARCHAR(1024)," +
                "status TINYINT NOT NULL DEFAULT 0," +
                "feedback_content TEXT," +
                "feedback_time DATETIME," +
                "feedback_by VARCHAR(64)," +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "is_deleted TINYINT NOT NULL DEFAULT 0)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_complaint_no ON t_complaint_record(complaint_no)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_cp_batch_no ON t_complaint_record(product_batch_no)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_cp_phone ON t_complaint_record(consumer_phone)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_cp_consumer_id ON t_complaint_record(consumer_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_cp_status ON t_complaint_record(status)");

            stmt.execute("DROP TABLE IF EXISTS t_consumer_info");
            stmt.execute("CREATE TABLE t_consumer_info (" +
                "consumer_id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "consumer_uuid VARCHAR(32)," +
                "phone VARCHAR(20) NOT NULL," +
                "nick_name VARCHAR(64)," +
                "real_name VARCHAR(64)," +
                "gender VARCHAR(8)," +
                "region VARCHAR(128)," +
                "last_scan_time DATETIME," +
                "total_scans INT NOT NULL DEFAULT 0," +
                "complaint_count INT NOT NULL DEFAULT 0," +
                "status TINYINT NOT NULL DEFAULT 1," +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "create_by VARCHAR(64)," +
                "update_by VARCHAR(64)," +
                "is_deleted TINYINT NOT NULL DEFAULT 0)");
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_consumer_phone ON t_consumer_info(phone)");
        }
    }
}

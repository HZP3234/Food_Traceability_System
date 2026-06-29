package com.foodtraceability.enterprise.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 数据库迁移 — 启动时自动执行 DDL 变更（幂等）
 */
@Component
public class DatabaseMigration implements ApplicationRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // 运输订单表：添加 sales_order_code 字段（如果不存在则添加）
            try {
                stmt.execute("ALTER TABLE t_cc_transport " +
                        "ADD COLUMN sales_order_code VARCHAR(32) DEFAULT NULL " +
                        "COMMENT '关联销售编码' AFTER prod_batch_no");
                System.out.println("[Migration] t_cc_transport.sales_order_code 列已添加");
            } catch (Exception e) {
                // 字段已存在时会报错，忽略即可（幂等）
                if (e.getMessage() != null && e.getMessage().contains("Duplicate column")) {
                    System.out.println("[Migration] t_cc_transport.sales_order_code 列已存在，跳过");
                } else {
                    System.out.println("[Migration] 跳过 t_cc_transport 迁移: " + e.getMessage());
                }
            }

        }
    }
}

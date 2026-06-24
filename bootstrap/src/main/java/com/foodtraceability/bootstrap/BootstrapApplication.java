package com.foodtraceability.bootstrap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 食品追溯系统 — 统一启动入口
 * <p>
 * 聚合 customers / enterprise / regulation 三个模块，
 * 启动本类即可运行整个系统。
 */
@SpringBootApplication(scanBasePackages = "com.foodtraceability")
@MapperScan({
    "com.foodtraceability.customers.mapper",
    "com.foodtraceability.enterprise.mapper",
    "com.foodtraceability.regulation.mapper"
})
public class BootstrapApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootstrapApplication.class, args);
    }

}

package com.foodtraceability.regulation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.foodtraceability.regulation.mapper")
public class RegulationApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegulationApplication.class, args);
    }

}

package com.foodtraceability.enterprise.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 溯源码管理模块配置
 * <p>
 * 可在 application.properties 中以 trace-code.* 覆盖默认值。
 * 例如：trace-code.batch-prefix=TC
 *
 * @author GuangYao Liu
 * @since 2026-06-23
 */
@Configuration
public class TraceCodeConfig {

    @Bean
    @ConfigurationProperties(prefix = "trace-code")
    public TraceCodeProperties traceCodeProperties() {
        return new TraceCodeProperties();
    }

    /**
     * 溯源码模块可配置属性
     */
    public static class TraceCodeProperties {

        /** 溯源码前缀（单品码） */
        private String singlePrefix = "TC";

        /** 批次码前缀 */
        private String batchPrefix = "BT";

        /** 箱码前缀 */
        private String boxPrefix = "BX";

        /** 托盘码前缀 */
        private String palletPrefix = "PL";

        /** Hash 算法 */
        private String hashAlgorithm = "SHA-256";

        /** 日期格式 */
        private String datePattern = "yyyyMMdd";

        /** 批量生成单次上限 */
        private int batchMaxLimit = 10000;

        /** 是否启用区块链存证（模拟） */
        private boolean blockchainEnabled = true;

        // ======== Getter / Setter ========

        public String getSinglePrefix() { return singlePrefix; }
        public void setSinglePrefix(String singlePrefix) { this.singlePrefix = singlePrefix; }

        public String getBatchPrefix() { return batchPrefix; }
        public void setBatchPrefix(String batchPrefix) { this.batchPrefix = batchPrefix; }

        public String getBoxPrefix() { return boxPrefix; }
        public void setBoxPrefix(String boxPrefix) { this.boxPrefix = boxPrefix; }

        public String getPalletPrefix() { return palletPrefix; }
        public void setPalletPrefix(String palletPrefix) { this.palletPrefix = palletPrefix; }

        public String getHashAlgorithm() { return hashAlgorithm; }
        public void setHashAlgorithm(String hashAlgorithm) { this.hashAlgorithm = hashAlgorithm; }

        public String getDatePattern() { return datePattern; }
        public void setDatePattern(String datePattern) { this.datePattern = datePattern; }

        public int getBatchMaxLimit() { return batchMaxLimit; }
        public void setBatchMaxLimit(int batchMaxLimit) { this.batchMaxLimit = batchMaxLimit; }

        public boolean isBlockchainEnabled() { return blockchainEnabled; }
        public void setBlockchainEnabled(boolean blockchainEnabled) { this.blockchainEnabled = blockchainEnabled; }
    }
}

package com.foodtraceability.enterprise.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 原料批次模块配置
 * <p>
 * 可在 application.properties 中以 raw-batch.* 覆盖默认值。
 */
@Configuration
public class RawConfig {

    @Bean
    @ConfigurationProperties(prefix = "raw-batch")
    public RawBatchProperties rawBatchProperties() {
        return new RawBatchProperties();
    }

    public static class RawBatchProperties {

        /** 批次号前缀 */
        private String batchPrefix = "RB";

        /** 待匹配编号前缀 */
        private String pendingPrefix = "SU";

        /** Hash 算法 */
        private String hashAlgorithm = "SHA-256";

        /** 是否启用区块链存证 */
        private boolean blockchainEnabled = true;

        /** 日期格式 */
        private String datePattern = "yyyyMMdd";

        // ======== Getter / Setter ========

        public String getBatchPrefix() { return batchPrefix; }
        public void setBatchPrefix(String batchPrefix) { this.batchPrefix = batchPrefix; }

        public String getPendingPrefix() { return pendingPrefix; }
        public void setPendingPrefix(String pendingPrefix) { this.pendingPrefix = pendingPrefix; }

        public String getHashAlgorithm() { return hashAlgorithm; }
        public void setHashAlgorithm(String hashAlgorithm) { this.hashAlgorithm = hashAlgorithm; }

        public boolean isBlockchainEnabled() { return blockchainEnabled; }
        public void setBlockchainEnabled(boolean blockchainEnabled) { this.blockchainEnabled = blockchainEnabled; }

        public String getDatePattern() { return datePattern; }
        public void setDatePattern(String datePattern) { this.datePattern = datePattern; }
    }
}

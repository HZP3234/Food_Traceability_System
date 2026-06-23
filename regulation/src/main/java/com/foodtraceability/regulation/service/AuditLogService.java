package com.foodtraceability.regulation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodtraceability.regulation.entity.AuditLog;

/**
 * 操作日志审计 Service (需求 3.2.11)
 */
public interface AuditLogService extends IService<AuditLog> {

    /**
     * 写入审计日志（自动计算Hash链）
     */
    AuditLog writeLog(AuditLog log);

    /**
     * 归档超过12个月的日志
     */
    int archiveOldLogs();

    /**
     * 校验日志Hash链完整性
     */
    boolean verifyLogChainIntegrity();

    /**
     * 标记异常日志
     */
    void markAbnormal(Long logId, String reason);
}

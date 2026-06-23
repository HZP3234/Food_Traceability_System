package com.foodtraceability.regulation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodtraceability.regulation.entity.AuditLog;
import com.foodtraceability.regulation.mapper.AuditLogMapper;
import com.foodtraceability.regulation.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 操作日志审计 Service 实现 (需求 3.2.11)
 */
@Slf4j
@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {

    @Override
    public AuditLog writeLog(AuditLog auditLog) {
        // 生成UUID
        if (auditLog.getLogUuid() == null) {
            auditLog.setLogUuid(UUID.randomUUID().toString().replace("-", ""));
        }

        // 获取上一条日志的Hash作为链式前驱
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AuditLog::getLogId).last("LIMIT 1");
        AuditLog lastLog = this.getOne(wrapper);

        String prevHash = lastLog != null ? lastLog.getLogHash() : "0000000000000000000000000000000000000000000000000000000000000000";
        auditLog.setOperationTime(LocalDateTime.now());

        // 计算当前日志Hash: SHA-256(prevHash + operatorId + actionType + targetId + afterData + timestamp)
        String raw = prevHash
                + auditLog.getOperatorId()
                + auditLog.getActionType()
                + (auditLog.getTargetId() != null ? auditLog.getTargetId() : "")
                + (auditLog.getAfterData() != null ? auditLog.getAfterData() : "")
                + auditLog.getOperationTime().toString();
        auditLog.setLogHash(sha256(raw));

        this.save(auditLog);
        log.info("审计日志写入: logId={}, operatorId={}, actionType={}",
                auditLog.getLogId(), auditLog.getOperatorId(), auditLog.getActionType());
        return auditLog;
    }

    @Override
    public int archiveOldLogs() {
        LocalDateTime twelveMonthsAgo = LocalDateTime.now().minusMonths(12);
        // 标记归档（实际可迁移到归档表，此处仅标记）
        LambdaUpdateWrapper<AuditLog> wrapper = new LambdaUpdateWrapper<>();
        wrapper.lt(AuditLog::getOperationTime, twelveMonthsAgo)
               .set(AuditLog::getUpdateTime, LocalDateTime.now());
        int count = this.getBaseMapper().update(null, wrapper);
        log.info("日志归档完成: {}条", count);
        return count;
    }

    @Override
    public boolean verifyLogChainIntegrity() {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(AuditLog::getLogId);
        List<AuditLog> logs = this.list(wrapper);

        if (logs == null || logs.size() <= 1) {
            return true;
        }

        for (int i = 1; i < logs.size(); i++) {
            AuditLog prev = logs.get(i - 1);
            AuditLog curr = logs.get(i);

            // 校验: 当前日志的log_hash重新计算验证
            String raw = prev.getLogHash()
                    + curr.getOperatorId()
                    + curr.getActionType()
                    + (curr.getTargetId() != null ? curr.getTargetId() : "")
                    + (curr.getAfterData() != null ? curr.getAfterData() : "")
                    + curr.getOperationTime().toString();

            if (!sha256(raw).equals(curr.getLogHash())) {
                log.warn("日志Hash链断裂或篡改: logId={}", curr.getLogId());
                return false;
            }
        }
        return true;
    }

    @Override
    public void markAbnormal(Long logId, String reason) {
        LambdaUpdateWrapper<AuditLog> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AuditLog::getLogId, logId)
               .set(AuditLog::getIsAbnormal, 1)
               .set(AuditLog::getAbnormalDesc, reason)
               .set(AuditLog::getUpdateTime, LocalDateTime.now());
        this.update(wrapper);
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256计算失败", e);
        }
    }
}

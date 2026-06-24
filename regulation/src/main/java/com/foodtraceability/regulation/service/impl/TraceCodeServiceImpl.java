package com.foodtraceability.regulation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodtraceability.regulation.entity.TraceCode;
import com.foodtraceability.regulation.mapper.RegulationTraceCodeMapper;
import com.foodtraceability.regulation.service.TraceCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 监管全链追溯 Service 实现 (需求 3.2.10)
 */
@Slf4j
@Service
public class TraceCodeServiceImpl extends ServiceImpl<RegulationTraceCodeMapper, TraceCode> implements TraceCodeService {

    @Override
    public TraceCode getByTraceCode(String traceCode) {
        LambdaQueryWrapper<TraceCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TraceCode::getTraceCode, traceCode);
        return this.getOne(wrapper);
    }

    @Override
    public List<TraceCode> listByBatchNo(String batchNo) {
        LambdaQueryWrapper<TraceCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TraceCode::getBatchNo, batchNo)
               .orderByDesc(TraceCode::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public List<TraceCode> listByEnterprise(String enterpriseUuid) {
        LambdaQueryWrapper<TraceCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TraceCode::getEnterpriseUuid, enterpriseUuid)
               .orderByDesc(TraceCode::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public boolean verifyContentHash(String traceCode) {
        TraceCode tc = getByTraceCode(traceCode);
        if (tc == null || tc.getContentHash() == null) {
            return false;
        }
        // 重新计算内容Hash与数据库中的content_hash比对
        String calculated = sha256(tc.getProductId() + tc.getBatchNo() + tc.getEnterpriseUuid());
        boolean valid = calculated.equals(tc.getContentHash());
        if (!valid) {
            log.warn("溯源码Hash不一致: traceCode={}", traceCode);
        }
        return valid;
    }

    @Override
    public void disableTraceCode(String traceCode, String reason) {
        LambdaUpdateWrapper<TraceCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TraceCode::getTraceCode, traceCode)
               .set(TraceCode::getTraceCodeStatus, 1) // 1-禁用
               .set(TraceCode::getDisableReason, reason)
               .set(TraceCode::getUpdateTime, LocalDateTime.now());
        this.update(wrapper);
        log.info("溯源码已禁用: traceCode={}, reason={}", traceCode, reason);
    }

    @Override
    public void voidTraceCode(String traceCode, String reason) {
        LambdaUpdateWrapper<TraceCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TraceCode::getTraceCode, traceCode)
               .set(TraceCode::getTraceCodeStatus, 2) // 2-作废
               .set(TraceCode::getVoidReason, reason)
               .set(TraceCode::getUpdateTime, LocalDateTime.now());
        this.update(wrapper);
        log.info("溯源码已作废: traceCode={}, reason={}", traceCode, reason);
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

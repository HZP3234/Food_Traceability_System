package com.foodtraceability.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.regulation.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志 Mapper — t_audit_log
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}

package com.foodtraceability.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.regulation.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {

    List<AuditLog> selectByOperatorId(@Param("operatorId") String operatorId);

    List<AuditLog> selectByActionType(@Param("actionType") Integer actionType);

    List<AuditLog> selectByIsAbnormal(@Param("isAbnormal") Integer isAbnormal);

    List<AuditLog> selectAllOrderByLogId();

    AuditLog selectLatestOne();

}
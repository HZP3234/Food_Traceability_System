package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.TraceCode;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface EnterpriseTraceCodeMapper extends BaseMapper<TraceCode> {

    TraceCode selectByTraceCode(@Param("code") String code);

    List<TraceCode> selectByBatchNo(@Param("batchNo") String batchNo);

    List<TraceCode> selectByEnterpriseId(@Param("enterpriseId") String enterpriseId);

    List<TraceCode> selectByTraceCodeStatus(@Param("status") Integer status);

    int countByTraceCode(@Param("code") String code);

}
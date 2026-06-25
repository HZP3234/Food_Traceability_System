package com.foodtraceability.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.regulation.entity.TraceCode;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface RegulationTraceCodeMapper extends BaseMapper<TraceCode> {

    TraceCode selectByTraceCode(@Param("code") String code);

    List<TraceCode> selectByBatchNo(@Param("batchNo") String batchNo);

    List<TraceCode> selectByEnterpriseUuid(@Param("uuid") String uuid);

    List<TraceCode> selectByTraceCodeStatus(@Param("status") Integer status);

}
package com.foodtraceability.customers.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.customers.entity.TraceCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TraceCodeMapper extends BaseMapper<TraceCode> {

    TraceCode selectByTraceCode(@Param("traceCode") String traceCode);
}

package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.TraceCodeBind;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface TraceCodeBindMapper extends BaseMapper<TraceCodeBind> {

    List<TraceCodeBind> selectByTraceCode(@Param("code") String code);

    List<TraceCodeBind> selectByBizType(@Param("bizType") Integer bizType);

}
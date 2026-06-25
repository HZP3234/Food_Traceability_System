package com.foodtraceability.customers.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface QualityInspectionMapper {

    List<Map<String, Object>> selectByBizBatchNo(@Param("bizBatchNo") String bizBatchNo);
}

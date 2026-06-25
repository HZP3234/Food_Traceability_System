package com.foodtraceability.customers.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface ProcessBatchMapper {

    Map<String, Object> selectByBatchNo(@Param("batchNo") String batchNo);
}

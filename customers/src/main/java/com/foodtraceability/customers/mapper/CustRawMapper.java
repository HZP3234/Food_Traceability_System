package com.foodtraceability.customers.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface CustRawMapper {

    Map<String, Object> selectByBatchNo(@Param("batchNo") String batchNo);

    java.util.List<Map<String, Object>> selectByProductName(@Param("productName") String productName);
}

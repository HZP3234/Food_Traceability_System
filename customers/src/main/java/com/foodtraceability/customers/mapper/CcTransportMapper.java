package com.foodtraceability.customers.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CcTransportMapper {

    List<Map<String, Object>> selectByProdBatchNo(@Param("prodBatchNo") String prodBatchNo);

    List<Map<String, Object>> selectByProductName(@Param("productName") String productName, @Param("prodBatchNo") String prodBatchNo);

    List<Map<String, Object>> selectByRawBatchNo(@Param("rawBatchNo") String rawBatchNo);
}

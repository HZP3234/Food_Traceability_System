package com.foodtraceability.customers.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CustSalesStockMapper {

    List<Map<String, Object>> selectByProductName(@Param("productName") String productName, @Param("prodBatchNo") String prodBatchNo);
}

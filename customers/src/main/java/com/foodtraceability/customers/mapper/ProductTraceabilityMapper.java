package com.foodtraceability.customers.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodtraceability.customers.entity.ProductTraceability;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductTraceabilityMapper extends BaseMapper<ProductTraceability> {

    ProductTraceability selectByBatchNo(@Param("batchNo") String batchNo);

    List<ProductTraceability> selectByProductName(@Param("productName") String productName);

    List<ProductTraceability> selectByManufacturer(@Param("manufacturer") String manufacturer);

    IPage<ProductTraceability> selectPageByKeyword(Page<ProductTraceability> page, @Param("keyword") String keyword);
}

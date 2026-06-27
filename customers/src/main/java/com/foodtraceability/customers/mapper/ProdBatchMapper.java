package com.foodtraceability.customers.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.customers.entity.ProdBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProdBatchMapper extends BaseMapper<ProdBatch> {

    ProdBatch selectByBatchNo(@Param("batchNo") String batchNo);

    ProdBatch selectByProductName(@Param("productName") String productName);
}

package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.ProdBatch;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProdBatchMapper extends BaseMapper<ProdBatch> {

    ProdBatch selectByBatchNo(@Param("batchNo") String batchNo);

    List<ProdBatch> selectByProcessBatchNo(@Param("processBatchNo") String processBatchNo);

    List<ProdBatch> selectByProductName(@Param("productName") String productName);

    List<ProdBatch> selectByBatchStatus(@Param("batchStatus") Integer batchStatus);

}
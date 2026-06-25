package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.ProcessBatch;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface EnterpriseProcessBatchMapper extends BaseMapper<ProcessBatch> {

    ProcessBatch selectByBatchNo(@Param("batchNo") String batchNo);

    List<ProcessBatch> selectByRawBatchNo(@Param("rawBatchNo") String rawBatchNo);

    List<ProcessBatch> selectByProductName(@Param("productName") String productName);

    List<ProcessBatch> selectByBatchStatus(@Param("batchStatus") Integer batchStatus);

}
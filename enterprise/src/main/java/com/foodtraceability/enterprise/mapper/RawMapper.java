package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.Raw;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface RawMapper extends BaseMapper<Raw> {

    Raw selectByBatchNo(@Param("batchNo") String batchNo);

    List<Raw> selectBySupplierId(@Param("supplierId") String supplierId);

    List<Raw> selectByProductName(@Param("productName") String productName);

    List<Raw> selectByBatchStatus(@Param("batchStatus") Integer batchStatus);

}
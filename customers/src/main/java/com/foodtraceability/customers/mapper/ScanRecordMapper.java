package com.foodtraceability.customers.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.customers.entity.ScanRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScanRecordMapper extends BaseMapper<ScanRecord> {

    List<ScanRecord> selectByProductBatchNo(@Param("batchNo") String batchNo);

    List<ScanRecord> selectByScanLocation(@Param("location") String location);
}

package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.RawDetail;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface RawDetailMapper extends BaseMapper<RawDetail> {

    RawDetail selectByBatchNo(@Param("batchNo") String batchNo);

    List<RawDetail> selectByOrigin(@Param("origin") String origin);

}
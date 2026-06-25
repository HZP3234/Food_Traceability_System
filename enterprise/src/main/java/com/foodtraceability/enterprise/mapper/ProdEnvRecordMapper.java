package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.ProdEnvRecord;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProdEnvRecordMapper extends BaseMapper<ProdEnvRecord> {

    List<ProdEnvRecord> selectByProductionLine(@Param("line") String line);

    List<ProdEnvRecord> selectByIsAbnormal(@Param("isAbnormal") Integer isAbnormal);

}
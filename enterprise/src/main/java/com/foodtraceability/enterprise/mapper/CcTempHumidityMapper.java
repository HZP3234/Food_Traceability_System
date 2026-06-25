package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.CcTempHumidity;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface CcTempHumidityMapper extends BaseMapper<CcTempHumidity> {

    List<CcTempHumidity> selectByOrderNo(@Param("orderNo") String orderNo);

    List<CcTempHumidity> selectByIsAbnormal(@Param("isAbnormal") Integer isAbnormal);

}
package com.foodtraceability.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.regulation.entity.Enterprise;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface EnterpriseMapper extends BaseMapper<Enterprise> {

    Enterprise selectByEnterpriseUuid(@Param("uuid") String uuid);

    List<Enterprise> selectByEnterpriseName(@Param("name") String name);

    List<Enterprise> selectByRiskLevel(@Param("riskLevel") Integer riskLevel);

    List<Enterprise> selectByEnterpriseType(@Param("type") Integer type);

}
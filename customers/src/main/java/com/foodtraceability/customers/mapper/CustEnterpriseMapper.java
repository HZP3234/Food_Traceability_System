package com.foodtraceability.customers.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.customers.entity.Enterprise;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CustEnterpriseMapper extends BaseMapper<Enterprise> {

    Enterprise selectByEnterpriseUuid(@Param("uuid") String uuid);
}

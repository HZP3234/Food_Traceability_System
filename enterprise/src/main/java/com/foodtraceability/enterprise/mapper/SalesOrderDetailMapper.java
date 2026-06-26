package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.SalesOrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SalesOrderDetailMapper extends BaseMapper<SalesOrderDetail> {

    SalesOrderDetail selectByDetailCode(@Param("code") String code);

    SalesOrderDetail selectByOrderCode(@Param("orderCode") String orderCode);

    List<SalesOrderDetail> selectByDetailStatus(@Param("status") Integer status);
}

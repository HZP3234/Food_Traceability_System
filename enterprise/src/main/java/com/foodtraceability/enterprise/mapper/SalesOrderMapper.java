package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.SalesOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SalesOrderMapper extends BaseMapper<SalesOrder> {

    SalesOrder selectByOrderCode(@Param("code") String code);

    List<SalesOrder> selectByBuyerName(@Param("buyerName") String buyerName);

    List<SalesOrder> selectByOrderStatus(@Param("status") Integer status);
}

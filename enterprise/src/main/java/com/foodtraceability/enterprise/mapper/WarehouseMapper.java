package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {

    Warehouse selectByWarehouseName(@Param("name") String name);

    Warehouse selectByWarehouseUuid(@Param("uuid") String uuid);

    List<Warehouse> selectByWarehouseType(@Param("type") Integer type);

    List<Warehouse> selectByWarehouseStatus(@Param("status") Integer status);

}
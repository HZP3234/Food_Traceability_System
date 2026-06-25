package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.CcVehicle;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface CcVehicleMapper extends BaseMapper<CcVehicle> {

    CcVehicle selectByPlateNo(@Param("plateNo") String plateNo);

    List<CcVehicle> selectByOwnerId(@Param("ownerId") String ownerId);

    List<CcVehicle> selectByVehicleStatus(@Param("status") Integer status);

    List<CcVehicle> selectByColdType(@Param("coldType") String coldType);

}
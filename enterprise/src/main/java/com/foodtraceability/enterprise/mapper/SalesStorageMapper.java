package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.SalesStorage;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface SalesStorageMapper extends BaseMapper<SalesStorage> {

    SalesStorage selectByTerminalCode(@Param("code") String code);

    List<SalesStorage> selectByProductType(@Param("type") String type);

}
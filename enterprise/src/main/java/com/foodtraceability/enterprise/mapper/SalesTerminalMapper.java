package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.SalesTerminal;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface SalesTerminalMapper extends BaseMapper<SalesTerminal> {

    SalesTerminal selectByTerminalCode(@Param("code") String code);

    List<SalesTerminal> selectByOperatorId(@Param("operatorId") String operatorId);

    List<SalesTerminal> selectByArea(@Param("area") String area);

    List<SalesTerminal> selectByTerminalStatus(@Param("status") Integer status);

}
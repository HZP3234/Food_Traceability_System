package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.SalesStock;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface SalesStockMapper extends BaseMapper<SalesStock> {

    List<SalesStock> selectByTerminalId(@Param("terminalId") String terminalId);

    List<SalesStock> selectByProdBatchNo(@Param("batchNo") String batchNo);

    List<SalesStock> selectByStockStatus(@Param("status") Integer status);

}
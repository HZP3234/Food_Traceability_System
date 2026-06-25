package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.SalesSupplement;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface SalesSupplementMapper extends BaseMapper<SalesSupplement> {

    List<SalesSupplement> selectByTraceBatchNo(@Param("batchNo") String batchNo);

    List<SalesSupplement> selectByTerminalCode(@Param("code") String code);

    List<SalesSupplement> selectBySupplementStatus(@Param("status") Integer status);

}
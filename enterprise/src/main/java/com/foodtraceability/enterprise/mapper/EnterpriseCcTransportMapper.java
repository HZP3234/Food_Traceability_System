package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.CcTransport;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface EnterpriseCcTransportMapper extends BaseMapper<CcTransport> {

    CcTransport selectByOrderNo(@Param("orderNo") String orderNo);

    List<CcTransport> selectByProdBatchNo(@Param("batchNo") String batchNo);

    List<CcTransport> selectByPlateNo(@Param("plateNo") String plateNo);

    List<CcTransport> selectByTransportStatus(@Param("status") Integer status);

}
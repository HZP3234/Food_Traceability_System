package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.RawPending;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface RawPendingMapper extends BaseMapper<RawPending> {

    RawPending selectByPendingCode(@Param("pendingCode") String pendingCode);

    List<RawPending> selectByPendingStatus(@Param("pendingStatus") Integer pendingStatus);

    List<RawPending> selectBySupplierName(@Param("supplierName") String supplierName);

}
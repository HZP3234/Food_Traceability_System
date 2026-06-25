package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.RawTransportPending;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RawTransportPendingMapper extends BaseMapper<RawTransportPending> {

    List<RawTransportPending> selectBySupplierName(@Param("supplierName") String supplierName);

    List<RawTransportPending> selectByMatchStatus(@Param("matchStatus") Integer matchStatus);

    List<RawTransportPending> selectByTransportOrderNo(@Param("transportOrderNo") String transportOrderNo);
}

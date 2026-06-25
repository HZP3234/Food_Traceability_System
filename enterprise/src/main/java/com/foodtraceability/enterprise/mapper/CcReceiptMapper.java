package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.CcReceipt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CcReceiptMapper extends BaseMapper<CcReceipt> {

    CcReceipt selectByOrderNo(@Param("orderNo") String orderNo);

}
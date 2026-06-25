package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.CcTransportNode;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface CcTransportNodeMapper extends BaseMapper<CcTransportNode> {

    List<CcTransportNode> selectByOrderNo(@Param("orderNo") String orderNo);

    List<CcTransportNode> selectByNodeType(@Param("nodeType") Integer nodeType);

}
package com.foodtraceability.customers.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.customers.entity.TraceabilityNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TraceabilityNodeMapper extends BaseMapper<TraceabilityNode> {

    List<TraceabilityNode> selectByProductBatchNo(@Param("batchNo") String batchNo);

    List<TraceabilityNode> selectByNodeName(@Param("nodeName") String nodeName);

    List<TraceabilityNode> selectByOperator(@Param("operator") String operator);
}

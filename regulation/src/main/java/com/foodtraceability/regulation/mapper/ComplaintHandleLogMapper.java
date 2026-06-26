package com.foodtraceability.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.regulation.entity.ComplaintHandleLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 投诉处理日志 Mapper
 */
@Mapper
public interface ComplaintHandleLogMapper extends BaseMapper<ComplaintHandleLog> {

    /** 按投诉编号查询处理日志（按时间正序） */
    List<ComplaintHandleLog> selectByComplaintNo(@Param("complaintNo") String complaintNo);

    /** 按操作人查询 */
    List<ComplaintHandleLog> selectByOperatorId(@Param("operatorId") Long operatorId);
}

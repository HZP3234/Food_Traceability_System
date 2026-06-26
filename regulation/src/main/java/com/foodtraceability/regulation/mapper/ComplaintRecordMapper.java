package com.foodtraceability.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.regulation.entity.ComplaintRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 投诉记录 Mapper
 */
@Mapper
public interface ComplaintRecordMapper extends BaseMapper<ComplaintRecord> {

    /** 按投诉编号查询 */
    ComplaintRecord selectByComplaintNo(@Param("complaintNo") String complaintNo);

    /** 按被投诉企业查询 */
    List<ComplaintRecord> selectByEnterpriseUuid(@Param("enterpriseUuid") String enterpriseUuid);

    /** 按投诉人查询 */
    List<ComplaintRecord> selectByConsumerUuid(@Param("consumerUuid") String consumerUuid);

    /** 按状态查询 */
    List<ComplaintRecord> selectByStatus(@Param("status") Integer status);

    /** 按批次号查询 */
    List<ComplaintRecord> selectByBatchNumber(@Param("batchNumber") String batchNumber);
}

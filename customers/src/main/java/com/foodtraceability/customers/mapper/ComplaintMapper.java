package com.foodtraceability.customers.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodtraceability.customers.entity.Complaint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ComplaintMapper extends BaseMapper<Complaint> {

    Complaint selectByComplaintNo(@Param("complaintNo") String complaintNo);

    List<Complaint> selectByConsumerUuid(@Param("consumerUuid") String consumerUuid);

    List<Complaint> selectByBatchNumber(@Param("batchNumber") String batchNumber);

    List<Complaint> selectByStatus(@Param("status") Integer status);

    IPage<Complaint> selectPageByConditions(Page<Complaint> page,
                                            @Param("complaintNo") String complaintNo,
                                            @Param("complaintType") Integer complaintType,
                                            @Param("status") Integer status,
                                            @Param("phone") String phone,
                                            @Param("enterpriseName") String enterpriseName,
                                            @Param("consumerUuid") String consumerUuid);
}

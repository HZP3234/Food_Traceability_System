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

    List<Complaint> selectByConsumerId(@Param("consumerId") Long consumerId);

    List<Complaint> selectByProductBatchNo(@Param("batchNo") String batchNo);

    List<Complaint> selectByStatus(@Param("status") Integer status);

    IPage<Complaint> selectPageByConditions(Page<Complaint> page,
                                            @Param("complaintNo") String complaintNo,
                                            @Param("complaintType") Integer complaintType,
                                            @Param("status") Integer status,
                                            @Param("consumerPhone") String consumerPhone,
                                            @Param("productName") String productName);
}

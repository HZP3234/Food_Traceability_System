package com.foodtraceability.customers.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodtraceability.customers.entity.Complaint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ComplaintMapper extends BaseMapper<Complaint> {

    Page<Complaint> selectComplaintPage(Page<Complaint> page,
                                        @Param("complaintNo") String complaintNo,
                                        @Param("productBatchNo") String productBatchNo,
                                        @Param("productName") String productName,
                                        @Param("consumerPhone") String consumerPhone,
                                        @Param("complaintType") Integer complaintType,
                                        @Param("status") Integer status);
}

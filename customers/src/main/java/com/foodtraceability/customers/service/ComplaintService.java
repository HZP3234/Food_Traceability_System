package com.foodtraceability.customers.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodtraceability.customers.dto.ComplaintQueryDTO;
import com.foodtraceability.customers.dto.ComplaintSubmitDTO;
import com.foodtraceability.customers.entity.Complaint;

public interface ComplaintService {

    /**
     * 提交投诉
     */
    Complaint submit(ComplaintSubmitDTO dto);

    /**
     * 分页查询投诉
     */
    Page<Complaint> page(ComplaintQueryDTO dto);

    /**
     * 查询投诉详情
     */
    Complaint detail(Long id);
}

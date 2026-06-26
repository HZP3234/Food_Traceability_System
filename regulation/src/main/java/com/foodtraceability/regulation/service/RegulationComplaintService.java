package com.foodtraceability.regulation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodtraceability.regulation.dto.ComplaintHandleDTO;
import com.foodtraceability.regulation.dto.ComplaintPageDTO;
import com.foodtraceability.regulation.dto.ComplaintStats;
import com.foodtraceability.regulation.entity.ComplaintHandleLog;
import com.foodtraceability.regulation.entity.ComplaintRecord;

import java.util.List;

/**
 * 投诉处理 Service (监管端)
 * <p>
 * 监管机构对消费者提交的投诉进行受理、调查、处理和关闭。
 * </p>
 */
public interface RegulationComplaintService extends IService<ComplaintRecord> {

    /**
     * 分页查询投诉列表（支持多条件筛选）
     */
    Page<ComplaintRecord> pageQuery(ComplaintPageDTO dto);

    /**
     * 获取投诉详情（含处理日志链路）
     */
    ComplaintRecord detail(Long complaintRecordId);

    /**
     * 获取投诉的处理日志
     */
    List<ComplaintHandleLog> getHandleLogs(String complaintNo);

    /**
     * 执行投诉处理操作（受理/调查/回复/关闭/驳回）
     */
    ComplaintRecord handle(ComplaintHandleDTO dto, String handlerName, Long operatorId);

    /**
     * 获取投诉统计概览
     */
    ComplaintStats getStats();
}

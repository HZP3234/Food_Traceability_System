package com.foodtraceability.regulation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodtraceability.regulation.entity.TraceCode;

import java.util.List;

/**
 * 监管全链追溯 Service (需求 3.2.10)
 */
public interface TraceCodeService extends IService<TraceCode> {

    /**
     * 按溯源码精确查询
     */
    TraceCode getByTraceCode(String traceCode);

    /**
     * 按批次号查询所有溯源码
     */
    List<TraceCode> listByBatchNo(String batchNo);

    /**
     * 按企业UUID查询溯源码
     */
    List<TraceCode> listByEnterprise(String enterpriseUuid);

    /**
     * 校验溯源码内容Hash完整性
     */
    boolean verifyContentHash(String traceCode);

    /**
     * 禁用溯源码
     */
    void disableTraceCode(String traceCode, String reason);

    /**
     * 作废溯源码
     */
    void voidTraceCode(String traceCode, String reason);

    /**
     * 恢复溯源码（从禁用/作废状态恢复为正常）
     */
    void restoreTraceCode(String traceCode);

    /**
     * 查询所有溯源码列表
     */
    List<TraceCode> listAll();

    /**
     * 按企业名称模糊查询溯源码
     */
    List<TraceCode> listByEnterpriseName(String enterpriseName);
}

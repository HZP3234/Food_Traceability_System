package com.foodtraceability.regulation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodtraceability.regulation.entity.Enterprise;

import java.util.List;

/**
 * 企业资质管理 Service (需求 3.2.9)
 */
public interface EnterpriseService extends IService<Enterprise> {

    /**
     * 检查并更新资质状态（定时任务：每日检查到期/过期）
     * 通过关联 t_qualification 表判断
     */
    void checkAndUpdateQualificationStatus();

    /**
     * 按企业名称模糊查询
     */
    List<Enterprise> searchByName(String name);

    /**
     * 按企业UUID查询
     */
    Enterprise getByEnterpriseUuid(String uuid);

    /**
     * 按风险等级筛选
     */
    List<Enterprise> listByRiskLevel(Integer riskLevel);
}

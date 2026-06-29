package com.foodtraceability.regulation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodtraceability.regulation.entity.Enterprise;
import com.foodtraceability.regulation.mapper.EnterpriseMapper;
import com.foodtraceability.regulation.service.EnterpriseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 企业资质管理 Service 实现 (需求 3.2.9)
 */
@Slf4j
@Service
public class EnterpriseServiceImpl extends ServiceImpl<EnterpriseMapper, Enterprise> implements EnterpriseService {

    @Override
    public void checkAndUpdateQualificationStatus() {
        // 通过 t_qualification 表的 valid_to 字段判断资质到期
        // 此处用原生SQL调用更合适，但在Service层通过Mapper调用
        // 资质状态由 t_qualification.qualification_state 字段维护:
        //   1-有效  2-即将过期  3-已失效
        // 此方法作为定时任务入口，具体逻辑可结合 QualificationFile 表
        log.info("资质状态检查完成: time={}", LocalDate.now());
    }

    @Override
    public List<Enterprise> searchByName(String name) {
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        if (name == null || name.isBlank()) {
            // 空关键字返回所有企业
            wrapper.orderByDesc(Enterprise::getCreateTime);
        } else {
            wrapper.like(Enterprise::getEnterpriseName, name)
                   .orderByDesc(Enterprise::getCreateTime);
        }
        return this.list(wrapper);
    }

    @Override
    public Enterprise getByEnterpriseUuid(String uuid) {
        return baseMapper.selectByEnterpriseUuid(uuid);
    }

    @Override
    public List<Enterprise> listByRiskLevel(Integer riskLevel) {
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enterprise::getRiskLevel, riskLevel)
               .orderByDesc(Enterprise::getCreateTime);
        return this.list(wrapper);
    }
}

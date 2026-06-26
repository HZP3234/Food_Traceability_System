package com.foodtraceability.regulation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodtraceability.regulation.dto.ComplaintHandleDTO;
import com.foodtraceability.regulation.dto.ComplaintPageDTO;
import com.foodtraceability.regulation.dto.ComplaintStats;
import com.foodtraceability.regulation.entity.ComplaintHandleLog;
import com.foodtraceability.regulation.entity.ComplaintRecord;
import com.foodtraceability.regulation.mapper.ComplaintHandleLogMapper;
import com.foodtraceability.regulation.mapper.ComplaintRecordMapper;
import com.foodtraceability.regulation.service.RegulationComplaintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 投诉处理 Service 实现 (监管端)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegulationComplaintServiceImpl extends ServiceImpl<ComplaintRecordMapper, ComplaintRecord> implements RegulationComplaintService {

    private final ComplaintRecordMapper complaintRecordMapper;
    private final ComplaintHandleLogMapper complaintHandleLogMapper;

    @Override
    public Page<ComplaintRecord> pageQuery(ComplaintPageDTO dto) {
        LambdaQueryWrapper<ComplaintRecord> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(dto.getComplaintNo() != null && !dto.getComplaintNo().isBlank(),
                     ComplaintRecord::getComplaintNo, dto.getComplaintNo())
               .like(dto.getEnterpriseName() != null && !dto.getEnterpriseName().isBlank(),
                     ComplaintRecord::getEnterpriseName, dto.getEnterpriseName())
               .like(dto.getComplainantName() != null && !dto.getComplainantName().isBlank(),
                     ComplaintRecord::getComplainantName, dto.getComplainantName())
               .eq(dto.getComplaintType() != null,
                   ComplaintRecord::getComplaintType, dto.getComplaintType())
               .eq(dto.getStatus() != null,
                   ComplaintRecord::getStatus, dto.getStatus())
               .eq(dto.getPriority() != null,
                   ComplaintRecord::getPriority, dto.getPriority())
               .ge(dto.getSubmitTimeStart() != null && !dto.getSubmitTimeStart().isBlank(),
                   ComplaintRecord::getSubmitTime, dto.getSubmitTimeStart() + " 00:00:00")
               .le(dto.getSubmitTimeEnd() != null && !dto.getSubmitTimeEnd().isBlank(),
                   ComplaintRecord::getSubmitTime, dto.getSubmitTimeEnd() + " 23:59:59")
               .orderByDesc(ComplaintRecord::getPriority)
               .orderByAsc(ComplaintRecord::getStatus)
               .orderByDesc(ComplaintRecord::getSubmitTime);

        Page<ComplaintRecord> page = new Page<>(dto.getPage(), dto.getSize());
        return complaintRecordMapper.selectPage(page, wrapper);
    }

    @Override
    public ComplaintRecord detail(Long complaintRecordId) {
        return complaintRecordMapper.selectById(complaintRecordId);
    }

    @Override
    public List<ComplaintHandleLog> getHandleLogs(String complaintNo) {
        return complaintHandleLogMapper.selectByComplaintNo(complaintNo);
    }

    @Override
    @Transactional
    public ComplaintRecord handle(ComplaintHandleDTO dto, String handlerName, Long operatorId) {
        ComplaintRecord record = complaintRecordMapper.selectById(dto.getComplaintRecordId());
        if (record == null) {
            throw new RuntimeException("投诉记录不存在");
        }

        String action = dto.getAction();
        if (action == null || action.isBlank()) {
            throw new RuntimeException("操作动作不能为空");
        }

        int beforeStatus = record.getStatus() != null ? record.getStatus() : 0;
        int afterStatus = beforeStatus;
        LocalDateTime now = LocalDateTime.now();

        switch (action.toUpperCase()) {
            case "ACCEPT":
                // 受理：状态 1→2
                if (beforeStatus != 1) {
                    throw new RuntimeException("只有「已提交」状态的投诉才能受理");
                }
                afterStatus = 2;
                record.setStatus(afterStatus);
                record.setHandlerId(operatorId != null ? String.valueOf(operatorId) : null);
                record.setHandlerName(handlerName);
                record.setAcceptTime(now);
                if (dto.getPriority() != null) {
                    record.setPriority(dto.getPriority());
                }
                break;

            case "INVESTIGATE":
                // 调查取证：状态保持 2，仅追加日志
                if (beforeStatus != 2) {
                    throw new RuntimeException("只有「处理中」状态的投诉才能进行调查");
                }
                afterStatus = beforeStatus; // 状态不变
                break;

            case "RESPOND":
                // 回复反馈：状态保持 2，仅追加日志
                if (beforeStatus != 2) {
                    throw new RuntimeException("只有「处理中」状态的投诉才能回复");
                }
                afterStatus = beforeStatus; // 状态不变
                break;

            case "CLOSE":
                // 办结关闭：状态 2→3
                if (beforeStatus != 2) {
                    throw new RuntimeException("只有「处理中」状态的投诉才能办结");
                }
                afterStatus = 3;
                record.setStatus(afterStatus);
                record.setHandlingConclusion(dto.getHandlingConclusion());
                record.setHandlingProofUrls(dto.getHandlingProofUrls());
                record.setCloseTime(now);
                break;

            case "REJECT":
                // 驳回：状态 1→4
                if (beforeStatus != 1) {
                    throw new RuntimeException("只有「已提交」状态的投诉才能驳回");
                }
                afterStatus = 4;
                record.setStatus(afterStatus);
                record.setHandlerId(operatorId != null ? String.valueOf(operatorId) : null);
                record.setHandlerName(handlerName);
                record.setHandlingConclusion(dto.getHandlingConclusion() != null
                    ? dto.getHandlingConclusion() : "投诉内容不属于受理范围，予以驳回");
                record.setCloseTime(now);
                break;

            default:
                throw new RuntimeException("不支持的操作类型: " + action);
        }

        record.setUpdateTime(now);
        complaintRecordMapper.updateById(record);

        // 写入处理日志
        ComplaintHandleLog handleLog = new ComplaintHandleLog();
        handleLog.setComplaintNo(record.getComplaintNo());
        handleLog.setAction(action.toUpperCase());
        handleLog.setBeforeStatus(beforeStatus);
        handleLog.setAfterStatus(afterStatus);
        handleLog.setRemark(dto.getRemark());
        handleLog.setOperatorId(operatorId);
        handleLog.setOperatorName(handlerName);
        handleLog.setOperatorRole(2); // 监管部门
        handleLog.setCreateTime(now);
        handleLog.setUpdateTime(now);
        handleLog.setCreateBy(handlerName);
        handleLog.setUpdateBy(handlerName);
        complaintHandleLogMapper.insert(handleLog);

        log.info("投诉处理: complaintNo={}, action={}, {}→{}, handler={}",
                record.getComplaintNo(), action, beforeStatus, afterStatus, handlerName);

        return record;
    }

    @Override
    public ComplaintStats getStats() {
        long total = complaintRecordMapper.selectCount(null);

        LambdaQueryWrapper<ComplaintRecord> w1 = new LambdaQueryWrapper<>();
        long pending = complaintRecordMapper.selectCount(
            w1.eq(ComplaintRecord::getStatus, 1));

        LambdaQueryWrapper<ComplaintRecord> w2 = new LambdaQueryWrapper<>();
        long processing = complaintRecordMapper.selectCount(
            w2.eq(ComplaintRecord::getStatus, 2));

        LambdaQueryWrapper<ComplaintRecord> w3 = new LambdaQueryWrapper<>();
        long resolved = complaintRecordMapper.selectCount(
            w3.eq(ComplaintRecord::getStatus, 3));

        LambdaQueryWrapper<ComplaintRecord> w4 = new LambdaQueryWrapper<>();
        long rejected = complaintRecordMapper.selectCount(
            w4.eq(ComplaintRecord::getStatus, 4));

        LambdaQueryWrapper<ComplaintRecord> w5 = new LambdaQueryWrapper<>();
        long urgent = complaintRecordMapper.selectCount(
            w5.in(ComplaintRecord::getPriority, 2, 3));

        return new ComplaintStats(total, pending, processing, resolved, rejected, urgent);
    }
}

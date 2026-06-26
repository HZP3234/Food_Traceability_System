package com.foodtraceability.customers.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodtraceability.customers.dto.ComplaintQueryDTO;
import com.foodtraceability.customers.dto.ComplaintSubmitDTO;
import com.foodtraceability.customers.entity.Complaint;
import com.foodtraceability.customers.mapper.ComplaintMapper;
import com.foodtraceability.customers.mapper.ConsumerMapper;
import com.foodtraceability.customers.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintMapper complaintMapper;
    private final ConsumerMapper consumerMapper;

    @Override
    @Transactional
    public Complaint submit(ComplaintSubmitDTO dto) {
        Complaint complaint = new Complaint();
        complaint.setComplaintNo(generateComplaintNo());
        complaint.setTraceCode(dto.getTraceCode());
        complaint.setBatchNumber(dto.getBatchNumber());
        complaint.setEnterpriseUuid(dto.getEnterpriseUuid());
        complaint.setEnterpriseName(dto.getEnterpriseName());
        complaint.setConsumerUuid(dto.getConsumerUuid());
        complaint.setComplainantName(dto.getComplainantName());
        complaint.setPhone(dto.getPhone());
        complaint.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : 0);
        complaint.setComplaintType(dto.getComplaintType());
        complaint.setDescription(dto.getDescription());
        complaint.setPhotoUrls(dto.getPhotoUrls());
        complaint.setPriority(1);
        complaint.setStatus(1); // 已提交
        complaint.setSubmitTime(LocalDateTime.now());

        complaintMapper.insert(complaint);

        if (dto.getConsumerUuid() != null && !dto.getConsumerUuid().isBlank()) {
            consumerMapper.incrementComplaintCount(dto.getConsumerUuid());
        }

        log.info("投诉提交成功: complaintNo={}, enterprise={}", complaint.getComplaintNo(), dto.getEnterpriseName());
        return complaint;
    }

    @Override
    public Page<Complaint> page(ComplaintQueryDTO dto) {
        Page<Complaint> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(dto.getComplaintNo()), Complaint::getComplaintNo, dto.getComplaintNo())
               .eq(StringUtils.hasText(dto.getBatchNumber()), Complaint::getBatchNumber, dto.getBatchNumber())
               .like(StringUtils.hasText(dto.getEnterpriseName()), Complaint::getEnterpriseName, dto.getEnterpriseName())
               .eq(dto.getComplaintType() != null, Complaint::getComplaintType, dto.getComplaintType())
               .eq(dto.getStatus() != null, Complaint::getStatus, dto.getStatus())
               .eq(StringUtils.hasText(dto.getConsumerUuid()), Complaint::getConsumerUuid, dto.getConsumerUuid());

        if (StringUtils.hasText(dto.getPhone())) {
            wrapper.and(w -> w.eq(Complaint::getPhone, dto.getPhone())
                    .or().like(Complaint::getPhone, dto.getPhone()));
        }

        wrapper.orderByDesc(Complaint::getCreateTime);
        return complaintMapper.selectPage(page, wrapper);
    }

    @Override
    public Complaint detail(Long id) {
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint == null) {
            throw new RuntimeException("投诉记录不存在");
        }
        return complaint;
    }

    private String generateComplaintNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = ThreadLocalRandom.current().nextInt(10000, 99999);
        return "TS" + date + seq;
    }
}

package com.foodtraceability.customers.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodtraceability.customers.dto.ComplaintFeedbackDTO;
import com.foodtraceability.customers.dto.ComplaintQueryDTO;
import com.foodtraceability.customers.dto.ComplaintSubmitDTO;
import com.foodtraceability.customers.entity.Complaint;
import com.foodtraceability.customers.entity.Consumer;
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
        complaint.setProductBatchNo(dto.getProductBatchNo());
        complaint.setProductName(dto.getProductName());
        complaint.setConsumerName(dto.getConsumerName());
        complaint.setConsumerPhone(dto.getConsumerPhone());
        complaint.setComplaintType(dto.getComplaintType());
        complaint.setComplaintTitle(dto.getComplaintTitle());
        complaint.setComplaintContent(dto.getComplaintContent());
        complaint.setImageUrls(dto.getImageUrls());
        complaint.setStatus(1);
        if (dto.getConsumerId() != null) {
            complaint.setConsumerId(dto.getConsumerId());
        } else if (dto.getConsumerPhone() != null && !dto.getConsumerPhone().isEmpty()) {
            Consumer consumer = consumerMapper.selectOne(
                    new LambdaQueryWrapper<Consumer>().eq(Consumer::getPhone, dto.getConsumerPhone()));
            if (consumer != null) {
                complaint.setConsumerId(consumer.getConsumerId());
            } else {
                log.warn("Consumer not found for phone: {}", dto.getConsumerPhone());
            }
        }
        complaintMapper.insert(complaint);
        return complaint;
    }

    @Override
    public Page<Complaint> page(ComplaintQueryDTO dto) {
        Page<Complaint> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(dto.getComplaintNo()), Complaint::getComplaintNo, dto.getComplaintNo())
                .eq(StringUtils.hasText(dto.getProductBatchNo()), Complaint::getProductBatchNo, dto.getProductBatchNo())
                .like(StringUtils.hasText(dto.getProductName()), Complaint::getProductName, dto.getProductName())
                .eq(dto.getComplaintType() != null, Complaint::getComplaintType, dto.getComplaintType())
                .eq(dto.getStatus() != null, Complaint::getStatus, dto.getStatus())
                .orderByDesc(Complaint::getCreateTime);
        // consumerId/consumerPhone OR logic
        if (dto.getConsumerId() != null && StringUtils.hasText(dto.getConsumerPhone())) {
            wrapper.and(w -> w.eq(Complaint::getConsumerId, dto.getConsumerId())
                    .or().eq(Complaint::getConsumerPhone, dto.getConsumerPhone()));
        } else if (dto.getConsumerId() != null) {
            wrapper.eq(Complaint::getConsumerId, dto.getConsumerId());
        } else if (StringUtils.hasText(dto.getConsumerPhone())) {
            wrapper.eq(Complaint::getConsumerPhone, dto.getConsumerPhone());
        }
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

    @Override
    @Transactional
    public Complaint feedback(ComplaintFeedbackDTO dto) {
        Complaint complaint = complaintMapper.selectById(dto.getComplaintId());
        if (complaint == null) {
            throw new RuntimeException("投诉记录不存在");
        }
        complaint.setFeedbackContent(dto.getFeedbackContent());
        complaint.setFeedbackBy(dto.getFeedbackBy());
        complaint.setFeedbackTime(LocalDateTime.now());
        complaint.setStatus(2);
        complaintMapper.updateById(complaint);
        return complaint;
    }

    private String generateComplaintNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = ThreadLocalRandom.current().nextInt(10000, 99999);
        return "TS" + date + seq;
    }
}

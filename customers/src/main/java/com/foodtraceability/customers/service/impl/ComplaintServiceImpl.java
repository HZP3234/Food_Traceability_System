package com.foodtraceability.customers.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodtraceability.customers.dto.ComplaintFeedbackDTO;
import com.foodtraceability.customers.dto.ComplaintQueryDTO;
import com.foodtraceability.customers.dto.ComplaintSubmitDTO;
import com.foodtraceability.customers.entity.Complaint;
import com.foodtraceability.customers.mapper.ComplaintMapper;
import com.foodtraceability.customers.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintMapper complaintMapper;

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
        complaint.setStatus(0);
        complaintMapper.insert(complaint);
        return complaint;
    }

    @Override
    public Page<Complaint> page(ComplaintQueryDTO dto) {
        Page<Complaint> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        return complaintMapper.selectComplaintPage(page,
                dto.getComplaintNo(),
                dto.getProductBatchNo(),
                dto.getProductName(),
                dto.getConsumerPhone(),
                dto.getComplaintType(),
                dto.getStatus());
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

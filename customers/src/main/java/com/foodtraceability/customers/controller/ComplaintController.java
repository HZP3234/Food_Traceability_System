package com.foodtraceability.customers.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodtraceability.customers.dto.ComplaintFeedbackDTO;
import com.foodtraceability.customers.dto.ComplaintQueryDTO;
import com.foodtraceability.customers.dto.ComplaintSubmitDTO;
import com.foodtraceability.customers.dto.Result;
import com.foodtraceability.customers.entity.Complaint;
import com.foodtraceability.customers.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaint")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping("/submit")
    public Result<Complaint> submit(@Valid @RequestBody ComplaintSubmitDTO dto) {
        Complaint complaint = complaintService.submit(dto);
        return Result.success(complaint);
    }

    @PostMapping("/page")
    public Result<Page<Complaint>> page(@RequestBody ComplaintQueryDTO dto) {
        Page<Complaint> page = complaintService.page(dto);
        return Result.success(page);
    }

    @GetMapping("/detail/{id}")
    public Result<Complaint> detail(@PathVariable Long id) {
        Complaint complaint = complaintService.detail(id);
        return Result.success(complaint);
    }

    @PutMapping("/feedback")
    public Result<Complaint> feedback(@Valid @RequestBody ComplaintFeedbackDTO dto) {
        Complaint complaint = complaintService.feedback(dto);
        return Result.success(complaint);
    }
}

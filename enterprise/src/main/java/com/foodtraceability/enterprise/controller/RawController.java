package com.foodtraceability.enterprise.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodtraceability.enterprise.entity.Raw;
import com.foodtraceability.enterprise.entity.RawDetail;
import com.foodtraceability.enterprise.entity.RawPending;
import com.foodtraceability.enterprise.service.RawService;

@RestController
@RequestMapping("/Raw")
public class RawController {
    @Autowired
    private RawService rawService;

    // ==================== t_raw_batch ====================

    // 按批次号查询
    @RequestMapping("/queryByBatchNo")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR', 'MANUFACTURER', 'SUPPLIER')")
    public Raw queryByBatchNo(String batchNo) {
        return rawService.getByBatchNo(batchNo);
    }

    // 条件列表查询
    @RequestMapping("/listRaw")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR', 'MANUFACTURER', 'SUPPLIER')")
    public List<Raw> listRaw(String supplierName, String productCategory,
                             Integer checkResult, String warehouse,
                             Integer batchStatus, Integer detailStatus) {
        return rawService.listRaw(supplierName, productCategory,
                checkResult, warehouse, batchStatus, detailStatus);
    }

    // 供应商视角：本公司相关批次
    @RequestMapping("/listBySupplierId")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPLIER')")
    public List<Raw> listBySupplierId(String supplierId) {
        return rawService.listBySupplierId(supplierId);
    }

    // 新增原料批次
    @RequestMapping("/createRaw")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String createRaw(Raw raw) {
        int num = rawService.createRaw(raw);
        if (num == 1) {
            return "原料批次创建成功";
        } else {
            return "原料批次创建失败";
        }
    }

    // 更新原料批次
    @RequestMapping("/updateRaw")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String updateRaw(Raw raw) {
        int num = rawService.updateRaw(raw);
        if (num == 1) {
            return "原料批次更新成功";
        } else {
            return "原料批次更新失败";
        }
    }

    // 软删除原料批次
    @RequestMapping("/deleteRaw")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String deleteRaw(Long rawBatchId) {
        int num = rawService.deleteRaw(rawBatchId);
        if (num == 1) {
            return "原料批次删除成功";
        } else {
            return "原料批次删除失败";
        }
    }

    // ==================== t_raw_detail ====================

    // 查询溯源详细信息
    @RequestMapping("/queryDetail")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR', 'MANUFACTURER', 'SUPPLIER')")
    public RawDetail queryDetail(String batchNo) {
        return rawService.getDetailByBatchNo(batchNo);
    }

    // 供应商上传溯源详细信息（自动匹配批次号）
    @RequestMapping("/uploadDetail")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPLIER')")
    public String uploadDetail(String batchNo, RawDetail detail) {
        int num = rawService.uploadDetail(batchNo, detail);
        if (num == 1) {
            return "溯源信息上传成功";
        } else {
            return "溯源信息上传失败";
        }
    }

    // ==================== 供应商主动上传 + 匹配 ====================

    // 主动上传原料信息（暂不匹配批次号）
    @RequestMapping("/proactiveUpload")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPLIER')")
    public String proactiveUpload(RawDetail detail, RawPending pending) {
        int num = rawService.proactiveUpload(detail, pending);
        if (num == 1) {
            return "原料信息已保存至待匹配列表";
        } else {
            return "上传失败";
        }
    }

    // 供应商匹配批次号
    @RequestMapping("/matchBatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPLIER')")
    public String matchBatch(String pendingCode, String targetBatchNo) {
        rawService.matchBatch(pendingCode, targetBatchNo);
        return "批次匹配成功";
    }

    // 查询待匹配列表（支持按供应商+状态组合过滤，不同供应商只能看到自己的数据）
    @RequestMapping("/listPending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPLIER', 'MANUFACTURER')")
    public List<RawPending> listPending(String supplierName, Integer pendingStatus) {
        // 当同时传入 supplierName 和 pendingStatus 时，使用组合查询
        if (supplierName != null && !supplierName.isBlank()) {
            return rawService.listPendingBySupplierAndStatus(supplierName, pendingStatus);
        }
        if (pendingStatus == null) pendingStatus = 1;
        return rawService.listPendingByStatus(pendingStatus);
    }

    // ==================== 质检 ====================

    // 录入质检结果
    @RequestMapping("/qualityCheck")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANUFACTURER')")
    public String qualityCheck(String batchNo, int checkResult) {
        rawService.recordQualityCheck(batchNo, checkResult);
        return "质检结果录入成功";
    }
}

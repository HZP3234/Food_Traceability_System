package com.foodtraceability.enterprise.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.foodtraceability.enterprise.entity.Raw;
import com.foodtraceability.enterprise.entity.RawDetail;
import com.foodtraceability.enterprise.entity.RawPending;
import com.foodtraceability.enterprise.mapper.RawMapper;
import com.foodtraceability.enterprise.mapper.RawDetailMapper;
import com.foodtraceability.enterprise.mapper.RawPendingMapper;

@Service
public class RawService {
    @Autowired
    private RawMapper rawMapper;
    @Autowired
    private RawDetailMapper rawDetailMapper;
    @Autowired
    private RawPendingMapper rawPendingMapper;

    // 生成原料批次号 RB + yyyyMMdd + 4位序号
    public String generateBatchNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seqPart = String.format("%04d", System.currentTimeMillis() % 10000);
        return "RB" + datePart + seqPart;
    }

    // 生成待匹配记录编号 SU + yyyyMMdd + 4位序号
    public String generatePendingCode() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seqPart = String.format("%04d", (System.currentTimeMillis() / 1000) % 10000);
        return "SU" + datePart + seqPart;
    }

    // ==================== t_raw_batch ====================

    // 根据批次号查询
    public Raw getByBatchNo(String batchNo) {
        QueryWrapper<Raw> qw = new QueryWrapper<>();
        qw.eq("batch_no", batchNo);
        qw.eq("is_deleted", 0);
        return rawMapper.selectOne(qw);
    }

    // 条件列表查询
    public List<Raw> listRaw(String supplierName, String productCategory,
                             Integer checkResult, String warehouse,
                             Integer batchStatus, Integer detailStatus) {
        QueryWrapper<Raw> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        if (supplierName != null && !supplierName.isBlank())
            qw.eq("supplier_name", supplierName);
        if (productCategory != null && !productCategory.isBlank())
            qw.eq("product_category", productCategory);
        if (checkResult != null)
            qw.eq("check_result", checkResult);
        if (warehouse != null && !warehouse.isBlank())
            qw.eq("warehouse", warehouse);
        if (batchStatus != null)
            qw.eq("batch_status", batchStatus);
        if (detailStatus != null)
            qw.eq("detail_status", detailStatus);
        qw.orderByDesc("create_time");
        return rawMapper.selectList(qw);
    }

    // 供应商视角：本公司相关批次
    public List<Raw> listBySupplierId(String supplierId) {
        QueryWrapper<Raw> qw = new QueryWrapper<>();
        qw.eq("supplier_id", supplierId);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("create_time");
        return rawMapper.selectList(qw);
    }

    // 新增原料批次
    public int createRaw(Raw raw) {
        if (raw.getBatchNo() == null || raw.getBatchNo().isBlank()) {
            raw.setBatchNo(generateBatchNo());
        }
        if (raw.getCheckResult() == null) raw.setCheckResult(2);
        if (raw.getBatchStatus() == null) raw.setBatchStatus(1);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (raw.getShelfLife() == null || raw.getShelfLife().isBlank()) raw.setShelfLife("2099-12-31");
        if (raw.getPurchaseDate() == null || raw.getPurchaseDate().isBlank()) raw.setPurchaseDate(now.substring(0, 10));
        if (raw.getDetailId() == null || raw.getDetailId().isBlank()) raw.setDetailId("0");
        if (raw.getDetailStatus() == null) raw.setDetailStatus(0);
        if (raw.getDataHash() == null) raw.setDataHash("");
        // 为 NOT NULL 且无数据库默认值的字段设默认值，防止前端未传参时插入失败
        if (raw.getProductName() == null || raw.getProductName().isBlank()) raw.setProductName("未命名原料");
        if (raw.getProductCategory() == null || raw.getProductCategory().isBlank()) raw.setProductCategory("未分类");
        if (raw.getSupplierId() == null || raw.getSupplierId().isBlank()) raw.setSupplierId("UNKNOWN");
        if (raw.getSupplierName() == null || raw.getSupplierName().isBlank()) raw.setSupplierName("未知供应商");
        if (raw.getWarehouse() == null || raw.getWarehouse().isBlank()) raw.setWarehouse("未指定仓库");
        raw.setCreateTime(now);
        raw.setUpdateTime(now);
        raw.setCreateBy(raw.getCreateBy() != null ? raw.getCreateBy() : "SYSTEM");
        raw.setUpdateBy(raw.getUpdateBy() != null ? raw.getUpdateBy() : "SYSTEM");
        return rawMapper.insert(raw);
    }

    // 更新原料批次
    public int updateRaw(Raw raw) {
        raw.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return rawMapper.updateById(raw);
    }

    // 软删除
    public int deleteRaw(Long rawBatchId) {
        Raw raw = rawMapper.selectById(rawBatchId);
        if (raw != null) {
            raw.setIsDeleted(1);
            return rawMapper.updateById(raw);
        }
        return 0;
    }

    // ==================== t_raw_detail ====================

    // 根据批次号查询溯源详细信息
    public RawDetail getDetailByBatchNo(String batchNo) {
        QueryWrapper<RawDetail> qw = new QueryWrapper<>();
        qw.eq("batch_no", batchNo);
        qw.eq("is_deleted", 0);
        return rawDetailMapper.selectOne(qw);
    }

    // 供应商上传溯源详细信息（自动匹配批次号）
    @Transactional
    public int uploadDetail(String batchNo, RawDetail detail) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        detail.setBatchNo(batchNo);
        detail.setUploadTime(now);
        detail.setCreateTime(now);
        detail.setUpdateTime(now);
        detail.setCreateBy(detail.getCreateBy() != null ? detail.getCreateBy() : "SYSTEM");
        detail.setUpdateBy(detail.getUpdateBy() != null ? detail.getUpdateBy() : "SYSTEM");
        int num = rawDetailMapper.insert(detail);

        // 回写原料批次表
        Raw raw = getByBatchNo(batchNo);
        if (raw != null) {
            raw.setDetailId(String.valueOf(detail.getRawDetailId()));
            raw.setDetailStatus(1);
            updateRaw(raw);
        }
        return num;
    }

    // 供应商主动上传原料信息（暂不匹配批次号）
    @Transactional
    public int proactiveUpload(RawDetail detail, RawPending pending) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // 生成临时批次号（batch_no 为 NOT NULL，匹配后替换为真实批次号）
        String tempBatchNo = "TMP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String operator = detail.getUploader() != null ? detail.getUploader() : "SYSTEM";
        detail.setBatchNo(tempBatchNo);
        detail.setUploadTime(now);
        detail.setCreateTime(now);
        detail.setUpdateTime(now);
        detail.setCreateBy(operator);
        detail.setUpdateBy(operator);
        rawDetailMapper.insert(detail);

        if (pending.getPendingCode() == null || pending.getPendingCode().isBlank()) {
            pending.setPendingCode(generatePendingCode());
        }
        pending.setRawDetailId(String.valueOf(detail.getRawDetailId()));
        pending.setMatchedBatchNo("");   // 待匹配时为空，匹配后更新
        pending.setPendingStatus(1);
        pending.setUploadTime(now);
        pending.setCreateTime(now);
        pending.setUpdateTime(now);
        pending.setCreateBy(operator);
        pending.setUpdateBy(operator);
        return rawPendingMapper.insert(pending);
    }

    // 供应商匹配批次号
    @Transactional
    public void matchBatch(String pendingCode, String targetBatchNo) {
        QueryWrapper<RawPending> qw = new QueryWrapper<>();
        qw.eq("pending_code", pendingCode);
        qw.eq("is_deleted", 0);
        RawPending pending = rawPendingMapper.selectOne(qw);
        if (pending == null) throw new RuntimeException("待匹配记录不存在: " + pendingCode);
        if (pending.getPendingStatus() != 1) throw new RuntimeException("该记录已匹配或已取消");

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        pending.setPendingStatus(2);
        pending.setMatchedBatchNo(targetBatchNo);
        pending.setMatchTime(now);
        rawPendingMapper.updateById(pending);

        // 安全解析 rawDetailId：RawPending.rawDetailId 为 String 类型
        try {
            long detailId = Long.parseLong(pending.getRawDetailId());
            RawDetail detail = rawDetailMapper.selectById(detailId);
            if (detail != null) {
                detail.setBatchNo(targetBatchNo);
                rawDetailMapper.updateById(detail);
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("待匹配记录的详情ID格式异常: " + pending.getRawDetailId(), e);
        }

        Raw raw = getByBatchNo(targetBatchNo);
        if (raw != null) {
            raw.setDetailId(pending.getRawDetailId());
            raw.setDetailStatus(1);
            updateRaw(raw);
        }
    }

    // ==================== 质检 ====================

    // 录入质检结果
    public void recordQualityCheck(String batchNo, int checkResult) {
        Raw raw = getByBatchNo(batchNo);
        if (raw != null) {
            raw.setCheckResult(checkResult);
            if (checkResult == 1) raw.setBatchStatus(2);
            updateRaw(raw);
        }
    }

    // ==================== t_raw_pending ====================

    // 按状态查询待匹配列表
    public List<RawPending> listPendingByStatus(int pendingStatus) {
        QueryWrapper<RawPending> qw = new QueryWrapper<>();
        qw.eq("pending_status", pendingStatus);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("upload_time");
        return rawPendingMapper.selectList(qw);
    }

    // 按供应商查询待匹配列表
    public List<RawPending> listPendingBySupplier(String supplierName) {
        QueryWrapper<RawPending> qw = new QueryWrapper<>();
        qw.eq("supplier_name", supplierName);
        qw.eq("pending_status", 1);
        qw.eq("is_deleted", 0);
        return rawPendingMapper.selectList(qw);
    }

    // 按供应商 + 状态组合查询待匹配列表
    public List<RawPending> listPendingBySupplierAndStatus(String supplierName, Integer pendingStatus) {
        QueryWrapper<RawPending> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        if (supplierName != null && !supplierName.isBlank()) {
            qw.eq("supplier_name", supplierName);
        }
        if (pendingStatus != null) {
            qw.eq("pending_status", pendingStatus);
        }
        qw.orderByDesc("upload_time");
        return rawPendingMapper.selectList(qw);
    }
}

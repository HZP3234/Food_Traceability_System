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
import com.foodtraceability.enterprise.mapper.RawTransportPendingMapper;
import com.foodtraceability.enterprise.util.CurrentUserUtil;
import com.foodtraceability.enterprise.entity.RawTransportPending;

@Service
public class RawService {
    @Autowired
    private RawMapper rawMapper;
    @Autowired
    private RawDetailMapper rawDetailMapper;
    @Autowired
    private RawPendingMapper rawPendingMapper;
    @Autowired
    private RawTransportPendingMapper rawTransportPendingMapper;
    @Autowired
    private CurrentUserUtil currentUserUtil;

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
        // 生产加工商只能查看自己创建的原料批次
        if (currentUserUtil.isManufacturer()) {
            qw.eq("create_by", currentUserUtil.getCurrentUsername());
        }
        return rawMapper.selectOne(qw);
    }

    // 条件列表查询
    public List<Raw> listRaw(String supplierName, String productCategory,
                             Integer checkResult, String warehouse,
                             Integer batchStatus, Integer detailStatus) {
        QueryWrapper<Raw> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        // 生产加工商只能查看自己创建的原料批次
        if (currentUserUtil.isManufacturer()) {
            qw.eq("create_by", currentUserUtil.getCurrentUsername());
        }
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

    // 新增原料批次（创建后自动匹配供应商待匹配记录）
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
        raw.setCreateBy(currentUserUtil.getCurrentUsername());
        raw.setUpdateBy(currentUserUtil.getCurrentUsername());
        int result = rawMapper.insert(raw);

        // ====== 自动匹配：新批次创建后，查找供应商已上传的待匹配原料 ======
        if (raw.getProductName() != null && !raw.getProductName().isBlank()
                && raw.getSupplierName() != null && !raw.getSupplierName().isBlank()) {
            QueryWrapper<RawPending> pendingQw = new QueryWrapper<>();
            pendingQw.eq("product_name", raw.getProductName());
            pendingQw.eq("supplier_name", raw.getSupplierName());
            pendingQw.eq("pending_status", 1);
            pendingQw.eq("is_deleted", 0);
            pendingQw.orderByAsc("upload_time");
            List<RawPending> pendings = rawPendingMapper.selectList(pendingQw);
            for (RawPending pending : pendings) {
                // 自动匹配
                pending.setPendingStatus(2);
                pending.setMatchedBatchNo(raw.getBatchNo());
                pending.setMatchTime(now);
                rawPendingMapper.updateById(pending);
                // 回写 detail
                try {
                    long detailId = Long.parseLong(pending.getRawDetailId());
                    RawDetail detail = rawDetailMapper.selectById(detailId);
                    if (detail != null) {
                        detail.setBatchNo(raw.getBatchNo());
                        rawDetailMapper.updateById(detail);
                    }
                } catch (NumberFormatException ignored) {}
                // 回写 raw 的详情关联（只关联第一个匹配到的）
                if (raw.getDetailStatus() == null || raw.getDetailStatus() == 0) {
                    raw.setDetailId(pending.getRawDetailId());
                    raw.setDetailStatus(1);
                    raw.setUpdateTime(now);
                    rawMapper.updateById(raw);
                }
            }
        }

        return result;
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

    // 供应商主动上传原料信息（自动匹配批次号）
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
        // 可为空的字段设默认值，避免数据库报错产生乱码
        if (detail.getOrigin() == null || detail.getOrigin().isBlank()) detail.setOrigin("未填写");
        if (detail.getFarmType() == null || detail.getFarmType().isBlank()) detail.setFarmType("未填写");
        if (detail.getFeedType() == null || detail.getFeedType().isBlank()) detail.setFeedType("未填写");
        if (detail.getInspectionNo() == null || detail.getInspectionNo().isBlank()) detail.setInspectionNo("未填写");
        if (detail.getBreed() == null || detail.getBreed().isBlank()) detail.setBreed("未填写");
        if (detail.getScaleDesc() == null || detail.getScaleDesc().isBlank()) detail.setScaleDesc("未填写");
        if (detail.getCollectDate() == null || detail.getCollectDate().isBlank())
            detail.setCollectDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        // t_raw_detail 表中 NOT NULL 无默认值的列：plate_no / transport_temp / storage_method / shelf_life
        if (detail.getPlateNo() == null || detail.getPlateNo().isBlank()) detail.setPlateNo("");
        if (detail.getTransportTemp() == null || detail.getTransportTemp().isBlank()) detail.setTransportTemp("");
        if (detail.getStorageMethod() == null) detail.setStorageMethod(0);
        if (detail.getShelfLife() == null || detail.getShelfLife().isBlank()) detail.setShelfLife("2099-12-31");
        if (detail.getRemark() == null) detail.setRemark("");
        rawDetailMapper.insert(detail);

        if (pending.getPendingCode() == null || pending.getPendingCode().isBlank()) {
            pending.setPendingCode(generatePendingCode());
        }
        pending.setRawDetailId(String.valueOf(detail.getRawDetailId()));
        pending.setPendingStatus(1);
        pending.setMatchedBatchNo("");  // 未匹配时置空，数据库NOT NULL无默认值；match_time 为 DATETIME 类型，不设值让 MyBatis-Plus 跳过 null
        pending.setUploadTime(now);
        pending.setCreateTime(now);
        pending.setUpdateTime(now);
        pending.setCreateBy(operator);
        pending.setUpdateBy(operator);
        if (pending.getProductCategory() == null || pending.getProductCategory().isBlank()) pending.setProductCategory("未填写");
        if (pending.getRemark() == null) pending.setRemark("");
        int result = rawPendingMapper.insert(pending);

        // ====== 自动匹配：按 产品名称 + 供应商名称 查找已有原料批次 ======
        if (pending.getProductName() != null && !pending.getProductName().isBlank()
                && pending.getSupplierName() != null && !pending.getSupplierName().isBlank()) {
            QueryWrapper<Raw> matchQw = new QueryWrapper<>();
            matchQw.eq("product_name", pending.getProductName());
            matchQw.eq("supplier_name", pending.getSupplierName());
            matchQw.eq("is_deleted", 0);
            matchQw.eq("detail_status", 0);  // 只匹配尚未上传详情的批次
            matchQw.orderByDesc("create_time");
            List<Raw> candidates = rawMapper.selectList(matchQw);
            if (!candidates.isEmpty()) {
                Raw matchedBatch = candidates.get(0);
                // 更新 pending 为已匹配状态
                pending.setPendingStatus(2);
                pending.setMatchedBatchNo(matchedBatch.getBatchNo());
                pending.setMatchTime(now);
                rawPendingMapper.updateById(pending);
                // 回写 detail 的真实批次号
                detail.setBatchNo(matchedBatch.getBatchNo());
                rawDetailMapper.updateById(detail);
                // 回写 raw 的详情关联
                matchedBatch.setDetailId(String.valueOf(detail.getRawDetailId()));
                matchedBatch.setDetailStatus(1);
                matchedBatch.setUpdateTime(now);
                rawMapper.updateById(matchedBatch);
            }
        }

        return result;
    }

    // 供应商为已存在的原料批次补充详细信息（加工商先创建批次，供应商后补充详情）
    @Transactional
    public int supplementDetail(RawDetail detail, String targetBatchNo,
                                String productName, String productCategory,
                                String supplierName) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String operator = detail.getUploader() != null ? detail.getUploader() : "SYSTEM";

        // 1. 创建 RawDetail，batchNo 直接使用真实批次号
        detail.setBatchNo(targetBatchNo);
        detail.setUploadTime(now);
        detail.setCreateTime(now);
        detail.setUpdateTime(now);
        detail.setCreateBy(operator);
        detail.setUpdateBy(operator);
        // 可为空的字段设默认值（与 proactiveUpload 保持一致）
        if (detail.getOrigin() == null || detail.getOrigin().isBlank()) detail.setOrigin("未填写");
        if (detail.getFarmType() == null || detail.getFarmType().isBlank()) detail.setFarmType("未填写");
        if (detail.getFeedType() == null || detail.getFeedType().isBlank()) detail.setFeedType("未填写");
        if (detail.getInspectionNo() == null || detail.getInspectionNo().isBlank()) detail.setInspectionNo("未填写");
        if (detail.getBreed() == null || detail.getBreed().isBlank()) detail.setBreed("未填写");
        if (detail.getScaleDesc() == null || detail.getScaleDesc().isBlank()) detail.setScaleDesc("未填写");
        if (detail.getCollectDate() == null || detail.getCollectDate().isBlank())
            detail.setCollectDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        if (detail.getPlateNo() == null || detail.getPlateNo().isBlank()) detail.setPlateNo("");
        if (detail.getTransportTemp() == null || detail.getTransportTemp().isBlank()) detail.setTransportTemp("");
        if (detail.getStorageMethod() == null) detail.setStorageMethod(0);
        if (detail.getShelfLife() == null || detail.getShelfLife().isBlank()) detail.setShelfLife("2099-12-31");
        if (detail.getRemark() == null) detail.setRemark("");
        rawDetailMapper.insert(detail);

        // 2. 创建 RawPending 审计记录（直接已匹配状态，保留完整溯源链路）
        RawPending pending = new RawPending();
        pending.setPendingCode(generatePendingCode());
        pending.setProductName(productName != null && !productName.isBlank() ? productName : "未知原料");
        pending.setProductCategory(productCategory != null && !productCategory.isBlank() ? productCategory : "未填写");
        pending.setSupplierName(supplierName);
        pending.setAmount(0);
        pending.setRawDetailId(String.valueOf(detail.getRawDetailId()));
        pending.setPendingStatus(2);           // 直接已匹配
        pending.setMatchedBatchNo(targetBatchNo);
        pending.setUploadTime(now);
        pending.setMatchTime(now);
        pending.setCreateTime(now);
        pending.setUpdateTime(now);
        pending.setCreateBy(operator);
        pending.setUpdateBy(operator);
        if (pending.getRemark() == null) pending.setRemark("");
        rawPendingMapper.insert(pending);

        // 3. 更新 Raw 批次：关联详情，状态改为已上传
        Raw raw = getByBatchNo(targetBatchNo);
        if (raw != null) {
            raw.setDetailId(String.valueOf(detail.getRawDetailId()));
            raw.setDetailStatus(1);
            raw.setUpdateTime(now);
            raw.setUpdateBy(operator);
            rawMapper.updateById(raw);
            return 1;
        }
        return 0;
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

    // ==================== t_raw_transport_pending ====================

    // 供应商上传运输单号供冷链匹配
    @Transactional
    public void uploadTransportPending(String rawDetailId, String transportOrderNo,
                                       String supplierName) {
        RawTransportPending tp = new RawTransportPending();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        tp.setRawDetailId(rawDetailId);
        tp.setTransportOrderNo(transportOrderNo);
        tp.setSupplierName(supplierName);
        tp.setMatchStatus(1);
        tp.setCreateTime(now);
        tp.setUpdateTime(now);
        tp.setCreateBy(supplierName != null ? supplierName : "SYSTEM");
        tp.setUpdateBy(supplierName != null ? supplierName : "SYSTEM");
        if (tp.getRemark() == null) tp.setRemark("");
        rawTransportPendingMapper.insert(tp);
    }

    // 查询运输待匹配列表
    public List<RawTransportPending> listTransportPending(
            String supplierName, Integer matchStatus) {
        if (supplierName != null && !supplierName.isBlank()) {
            QueryWrapper<RawTransportPending> qw = new QueryWrapper<>();
            qw.eq("supplier_name", supplierName);
            qw.eq("is_deleted", 0);
            if (matchStatus != null) qw.eq("match_status", matchStatus);
            qw.orderByDesc("create_time");
            return rawTransportPendingMapper.selectList(qw);
        }
        if (matchStatus != null) {
            return rawTransportPendingMapper.selectByMatchStatus(matchStatus);
        }
        QueryWrapper<RawTransportPending> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        qw.orderByDesc("create_time");
        return rawTransportPendingMapper.selectList(qw);
    }

    // 冷链模块匹配运输单号（由 ColdChainService 调用）
    public void matchTransportPending(String transportOrderNo, String rawBatchNo) {
        List<RawTransportPending> list =
                rawTransportPendingMapper.selectByTransportOrderNo(transportOrderNo);
        if (list != null && !list.isEmpty()) {
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            for (RawTransportPending tp : list) {
                if (tp.getMatchStatus() == 1) {
                    tp.setMatchStatus(2);
                    tp.setRawBatchNo(rawBatchNo);
                    tp.setMatchTime(now);
                    rawTransportPendingMapper.updateById(tp);
                }
            }
        }
    }
}

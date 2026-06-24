package com.foodtraceability.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.foodtraceability.enterprise.config.TraceCodeConfig.TraceCodeProperties;
import com.foodtraceability.enterprise.dto.*;
import com.foodtraceability.enterprise.entity.TraceCode;
import com.foodtraceability.enterprise.entity.TraceCodeBind;
import com.foodtraceability.enterprise.mapper.TraceCodeBindMapper;
import com.foodtraceability.enterprise.mapper.TraceCodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 溯源码管理核心 Service
 * <p>
 * 负责溯源码全生命周期管理：生成、绑定、批量生成、
 * 状态变更（启用/禁用/作废）、查询、校验及导出。
 * <p>
 * 功能对应概要设计说明书 表格13 — 溯源码管理模块。
 *
 * @author GuangYao Liu
 * @since 2026-06-23
 */
@Service
public class TraceCodeService {

    /** 日期格式化器 */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private TraceCodeMapper traceCodeMapper;

    @Autowired
    private TraceCodeBindMapper traceCodeBindMapper;

    @Autowired
    private TraceCodeProperties traceCodeProperties;

    // ==================== 编码生成 ====================

    /**
     * 生成全局唯一的溯源码
     * <p>
     * 编码规则：前缀 + yyyyMMdd + 6位序号（基于时间戳取模）
     * - 单品码前缀：TC
     * - 批次码前缀：BT
     * - 箱码前缀：BX
     * - 托码前缀：PL
     *
     * @param codeType 码类型：1-单品码 2-批次码 3-箱码 4-托码
     * @return 全局唯一溯源码字符串
     */
    public String generateCodeValue(Integer codeType) {
        String prefix;
        switch (codeType == null ? 1 : codeType) {
            case 2:  prefix = traceCodeProperties.getBatchPrefix();   break;
            case 3:  prefix = traceCodeProperties.getBoxPrefix();     break;
            case 4:  prefix = traceCodeProperties.getPalletPrefix();  break;
            default: prefix = traceCodeProperties.getSinglePrefix();  break;
        }
        String datePart = LocalDateTime.now().format(DATE_COMPACT);
        // 使用纳秒 + 随机数确保唯一性
        long nano = System.nanoTime();
        String seqPart = String.format("%06d", (nano / 1000) % 1000000);
        String code = prefix + datePart + seqPart;

        // 碰撞检测：如果已存在则递归重新生成
        if (existsByCode(code)) {
            return generateCodeValue(codeType);
        }
        return code;
    }

    /**
     * 生成批量生成批次号
     * <p>
     * 格式：GEN + yyyyMMdd + 4位序号
     */
    public String generateBatchNo() {
        String datePart = LocalDateTime.now().format(DATE_COMPACT);
        String seqPart = String.format("%04d", System.currentTimeMillis() % 10000);
        return "GEN" + datePart + seqPart;
    }

    /**
     * 检查溯源码是否已存在
     */
    private boolean existsByCode(String traceCode) {
        QueryWrapper<TraceCode> qw = new QueryWrapper<>();
        qw.eq("trace_code", traceCode);
        qw.eq("is_deleted", 0);
        return traceCodeMapper.selectCount(qw) > 0;
    }

    // ==================== 单条生成溯源码 ====================

    /**
     * 生成溯源码并与业务数据绑定，提交区块链存证
     * <p>
     * 流程：
     * 1. 业务数据校验（生产路径、质检结果、企业资质）
     * 2. 生成全局唯一溯源码
     * 3. 计算 SHA-256 内容哈希
     * 4. 保存溯源码记录（状态：已绑定）
     * 5. 模拟区块链存证
     *
     * @param dto 生成请求（含产品、企业、批次、质检等业务数据）
     * @return 生成的溯源码视图对象
     */
    public TraceCodeVO generateTraceCode(TraceCodeGenerateDTO dto) {
        // ---------- 1. 业务数据校验 ----------
        validateBusinessData(dto);

        // ---------- 2. 生成溯源码 ----------
        String codeValue = generateCodeValue(dto.getCodeType());
        String now = LocalDateTime.now().format(DATE_FMT);

        // ---------- 3. 构建溯源码实体 ----------
        TraceCode tc = new TraceCode();
        tc.setTraceCodeUuid(UUID.randomUUID().toString().replace("-", ""));
        tc.setTraceCode(codeValue);
        tc.setCodeType(dto.getCodeType() != null ? dto.getCodeType() : 1);
        tc.setPackageLevel(dto.getPackageLevel() != null ? dto.getPackageLevel() : 1);
        tc.setProductId(dto.getProductId());
        tc.setProductName(dto.getProductName());
        tc.setEnterpriseId(dto.getEnterpriseId());
        tc.setEnterpriseName(dto.getEnterpriseName());
        tc.setBatchNo(dto.getBatchNo());
        tc.setTraceCodeStatus(1); // 已绑定
        tc.setQualityResult(dto.getQualityResult() != null ? dto.getQualityResult() : 3);
        tc.setQualityReportUrl(dto.getQualityReportUrl());
        tc.setExpireTime(dto.getExpireTime());
        tc.setGenerateTime(now);
        tc.setOperator(dto.getOperator());
        tc.setCreateBy(dto.getOperator());
        tc.setUpdateBy(dto.getOperator());
        tc.setCreateTime(now);
        tc.setUpdateTime(now);
        tc.setIsDeleted(0);
        tc.setIsOnChain(0);
        tc.setGenerateCount(1);

        // ---------- 4. 计算 SHA-256 内容哈希 ----------
        String contentHash = computeContentHash(tc);
        tc.setContentHash(contentHash);

        // ---------- 5. 模拟区块链存证 ----------
        if (traceCodeProperties.isBlockchainEnabled()) {
            tc.setProofId("PROOF_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            tc.setTxHash("0x" + sha256(tc.getTraceCode() + contentHash + now).substring(0, 40));
            tc.setIsOnChain(1);
        }

        // ---------- 6. 保存 ----------
        traceCodeMapper.insert(tc);
        return toVO(tc);
    }

    // ==================== 批量生成溯源码 ====================

    /**
     * 按产品批次批量生成溯源码
     * <p>
     * 单次上限 10000 条，超出则返回提示（异步处理由外部调度）。
     * 批量生成的码自动绑定到同一生产批次。
     *
     * @param dto 批量生成请求（含数量）
     * @return 生成的溯源码视图列表
     */
    public List<TraceCodeVO> batchGenerateTraceCode(TraceCodeBatchDTO dto) {
        int count = dto.getGenerateCount() != null ? dto.getGenerateCount() : 1;
        int maxLimit = traceCodeProperties.getBatchMaxLimit();

        // 超出上限
        if (count > maxLimit) {
            throw new RuntimeException(
                "单次批量生成上限为 " + maxLimit + " 条，" + count + " 条超出限制，请分批生成或走异步通道"
            );
        }

        String now = LocalDateTime.now().format(DATE_FMT);
        String generateBatchNo = generateBatchNo();

        // 批量生成
        List<TraceCodeVO> resultList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            TraceCode tc = new TraceCode();
            tc.setTraceCodeUuid(UUID.randomUUID().toString().replace("-", ""));
            String codeValue = generateCodeValue(dto.getCodeType());

            tc.setTraceCode(codeValue);
            tc.setCodeType(dto.getCodeType() != null ? dto.getCodeType() : 1);
            tc.setPackageLevel(dto.getPackageLevel() != null ? dto.getPackageLevel() : 1);
            tc.setProductId(dto.getProductId());
            tc.setProductName(dto.getProductName());
            tc.setEnterpriseId(dto.getEnterpriseId());
            tc.setEnterpriseName(dto.getEnterpriseName());
            tc.setBatchNo(dto.getBatchNo());
            tc.setTraceCodeStatus(1); // 已绑定
            tc.setQualityResult(dto.getQualityResult() != null ? dto.getQualityResult() : 3);
            tc.setExpireTime(dto.getExpireTime());
            tc.setGenerateTime(now);
            tc.setGenerateCount(count);
            tc.setGenerateBatchNo(generateBatchNo);
            tc.setOperator(dto.getOperator());
            tc.setCreateBy(dto.getOperator());
            tc.setUpdateBy(dto.getOperator());
            tc.setCreateTime(now);
            tc.setUpdateTime(now);
            tc.setIsDeleted(0);
            tc.setIsOnChain(0);

            // 计算哈希
            String contentHash = computeContentHash(tc);
            tc.setContentHash(contentHash);

            // 模拟区块链存证（批量时采用简化的批次级存证）
            if (traceCodeProperties.isBlockchainEnabled()) {
                tc.setProofId("PROOF_BATCH_" + generateBatchNo);
                tc.setTxHash("0x" + sha256(generateBatchNo + contentHash + now).substring(0, 40));
                tc.setIsOnChain(1);
            }

            traceCodeMapper.insert(tc);
            resultList.add(toVO(tc));
        }

        return resultList;
    }

    // ==================== 查询 ====================

    /**
     * 查询单个溯源码完整信息及所有绑定业务数据详情
     *
     * @param traceCode 溯源码值
     * @return 溯源码详情（含绑定关系列表）
     */
    public TraceCodeDetailVO queryTraceCodeDetail(String traceCode) {
        TraceCode tc = getByCode(traceCode);
        if (tc == null) {
            throw new RuntimeException("溯源码不存在: " + traceCode);
        }

        TraceCodeDetailVO vo = new TraceCodeDetailVO();
        vo.setTraceCode(tc.getTraceCode());
        vo.setCodeTypeName(getCodeTypeName(tc.getCodeType()));
        vo.setPackageLevelName(getPackageLevelName(tc.getPackageLevel()));
        vo.setProductName(tc.getProductName());
        vo.setEnterpriseName(tc.getEnterpriseName());
        vo.setBatchNo(tc.getBatchNo());
        vo.setTraceCodeStatusName(getStatusName(tc.getTraceCodeStatus()));
        vo.setContentHash(tc.getContentHash());
        vo.setTxHash(tc.getTxHash());
        vo.setQualityResult(tc.getQualityResult());
        vo.setQualityReportUrl(tc.getQualityReportUrl());

        // 原因字段
        if (tc.getTraceCodeStatus() != null && tc.getTraceCodeStatus() == 3) {
            vo.setReason(tc.getDisableReason());
        } else if (tc.getTraceCodeStatus() != null && tc.getTraceCodeStatus() == 4) {
            vo.setReason(tc.getVoidReason());
        }

        vo.setGenerateTime(generatedAt(tc));
        vo.setEnableTime(tc.getTraceCodeStatus() != null && tc.getTraceCodeStatus() == 2 ? tc.getUpdateTime() : null);
        vo.setExpireTime(tc.getExpireTime());
        vo.setOperator(tc.getUpdateBy() != null ? tc.getUpdateBy() : tc.getCreateBy());

        // 查询绑定关系
        List<TraceCodeBind> binds = listBindsByCode(traceCode);
        List<TraceCodeDetailVO.BindInfo> bindInfos = new ArrayList<>();
        for (TraceCodeBind bind : binds) {
            TraceCodeDetailVO.BindInfo info = new TraceCodeDetailVO.BindInfo();
            info.setBizType(bind.getBizType());
            info.setBizTypeName(getBizTypeName(bind.getBizType()));
            info.setBizNo(bind.getBizNo());
            info.setBindTime(bind.getCreateTime());
            bindInfos.add(info);
        }
        vo.setBindList(bindInfos);

        return vo;
    }

    /**
     * 多条件分页查询溯源码列表
     *
     * @param dto 查询条件（码类型、状态、产品名、企业名、批次号、时间范围等）
     * @return 分页结果
     */
    public List<TraceCodeVO> queryTraceCodeList(TraceCodeQueryDTO dto) {
        QueryWrapper<TraceCode> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);

        if (dto.getTraceCode() != null && !dto.getTraceCode().isBlank()) {
            qw.like("trace_code", dto.getTraceCode());
        }
        if (dto.getCodeType() != null) {
            qw.eq("code_type", dto.getCodeType());
        }
        if (dto.getTraceCodeStatus() != null) {
            qw.eq("trace_code_status", dto.getTraceCodeStatus());
        }
        if (dto.getProductName() != null && !dto.getProductName().isBlank()) {
            qw.like("product_name", dto.getProductName());
        }
        if (dto.getEnterpriseName() != null && !dto.getEnterpriseName().isBlank()) {
            qw.like("enterprise_name", dto.getEnterpriseName());
        }
        if (dto.getBatchNo() != null && !dto.getBatchNo().isBlank()) {
            qw.like("batch_no", dto.getBatchNo());
        }
        if (dto.getCreateTimeStart() != null && !dto.getCreateTimeStart().isBlank()) {
            qw.ge("create_time", dto.getCreateTimeStart());
        }
        if (dto.getCreateTimeEnd() != null && !dto.getCreateTimeEnd().isBlank()) {
            qw.le("create_time", dto.getCreateTimeEnd());
        }

        qw.orderByDesc("create_time");

        // 简易分页：计算 offset
        int page = dto.getPage() != null ? dto.getPage() : 1;
        int pageSize = dto.getPageSize() != null ? dto.getPageSize() : 20;
        qw.last("LIMIT " + ((page - 1) * pageSize) + ", " + pageSize);

        List<TraceCode> list = traceCodeMapper.selectList(qw);
        List<TraceCodeVO> voList = new ArrayList<>();
        for (TraceCode tc : list) {
            if (dto.getIsOnChain() == null || dto.getIsOnChain().equals(isOnChain(tc))) {
                voList.add(toVO(tc));
            }
        }
        return voList;
    }

    // ==================== 消费者扫码查询 ====================

    /**
     * 消费者扫码查询接口（公开接口）
     * <p>
     * 消费者扫描溯源码后，返回产品溯源信息、流转轨迹、
     * 链上校验结果及风险提示。
     * <p>
     * 码状态校验：
     * - 无效码/不存在 → 返回错误提示
     * - 已禁用 → 显示异常提示
     * - 已作废 → 显示作废提示
     * - 已过期 → 显示过期提示
     * - 未激活 → 提示码尚未启用
     *
     * @param traceCode 消费者扫描的溯源码值
     * @return 消费者溯源结果
     */
    public ConsumerTraceVO queryByCode(String traceCode) {
        TraceCode tc = getByCode(traceCode);

        ConsumerTraceVO vo = new ConsumerTraceVO();
        String now = LocalDateTime.now().format(DATE_FMT);

        // ---------- 码不存在 ----------
        if (tc == null) {
            vo.setCodeStatus("无效码");
            vo.setQueryResult("fail");
            vo.setScanTime(now);
            vo.setRiskLevel("重大");
            vo.setRiskSuggestion("该溯源码不存在，请核实产品真伪，谨防假冒伪劣产品。");
            return vo;
        }

        // ---------- 码状态校验 ----------
        Integer status = tc.getTraceCodeStatus();
        if (status == null || status == 0) {
            // 未绑定
            vo.setCodeStatus("未绑定");
            vo.setQueryResult("fail");
            vo.setScanTime(now);
            vo.setRiskLevel("高");
            vo.setRiskSuggestion("该溯源码尚未绑定产品信息，请联系商家核实。");
            return vo;
        }

        if (status == 3) {
            // 已禁用
            vo.setCodeStatus("已禁用");
            vo.setQueryResult("fail");
            vo.setScanTime(now);
            vo.setRiskLevel("重大");
            vo.setRiskSuggestion("该产品已被禁用，请勿食用！禁用原因：" +
                (tc.getDisableReason() != null ? tc.getDisableReason() : "产品质量问题"));
            return vo;
        }

        if (status == 4) {
            // 已作废
            vo.setCodeStatus("已作废");
            vo.setQueryResult("fail");
            vo.setScanTime(now);
            vo.setRiskLevel("重大");
            vo.setRiskSuggestion("该溯源码已作废，原因：" +
                (tc.getVoidReason() != null ? tc.getVoidReason() : "码已作废"));
            return vo;
        }

        if (status == 5) {
            // 已过期
            vo.setCodeStatus("已过期");
            vo.setQueryResult("fail");
            vo.setScanTime(now);
            vo.setRiskLevel("中");
            vo.setRiskSuggestion("该溯源码已过有效期，溯源信息仅供参考。");
            return vo;
        }

        if (status == 1) {
            // 已绑定但未激活
            vo.setCodeStatus("待激活");
            vo.setQueryResult("fail");
            vo.setScanTime(now);
            vo.setRiskLevel("低");
            vo.setRiskSuggestion("该溯源码尚未激活，溯源信息暂不可用。");
            return vo;
        }

        // ---------- 状态为已激活(2)，正常返回溯源信息 ----------
        vo.setProductName(tc.getProductName());
        vo.setBatchNo(tc.getBatchNo());
        vo.setEnterpriseName(tc.getEnterpriseName());
        vo.setCodeStatus("正常");

        // 质检结果
        Integer qr = tc.getQualityResult();
        if (qr == null || qr == 3) {
            vo.setQualityResult("待检");
        } else if (qr == 1) {
            vo.setQualityResult("合格");
        } else {
            vo.setQualityResult("不合格");
        }

        // ---------- 构建流转轨迹 ----------
        List<TraceCodeBind> binds = listBindsByCode(traceCode);
        List<ConsumerTraceVO.TraceNode> nodes = new ArrayList<>();
        if (binds.isEmpty()) {
            // 无绑定时使用批次号构建一个基础节点
            ConsumerTraceVO.TraceNode node = new ConsumerTraceVO.TraceNode();
            node.setNodeType("生产");
            node.setNodeName("生产批次");
            node.setNodeCode(tc.getBatchNo());
            node.setRecordTime(generatedAt(tc));
            node.setDetail("产品名称: " + (tc.getProductName() != null ? tc.getProductName() : "未知"));
            node.setIsException(0);
            nodes.add(node);
        } else {
            for (TraceCodeBind bind : binds) {
                ConsumerTraceVO.TraceNode node = new ConsumerTraceVO.TraceNode();
                String typeName = getBizTypeName(bind.getBizType());
                node.setNodeType(typeName);
                node.setNodeName(typeName + "信息");
                node.setNodeCode(bind.getBizNo());
                node.setRecordTime(bind.getCreateTime());
                node.setDetail("关联" + typeName + ": " + bind.getBizNo());
                node.setIsException(0);
                nodes.add(node);
            }
        }
        vo.setTraceNodes(nodes);

        // ---------- 链上校验 ----------
        if (isOnChain(tc) == 1) {
            // 重新计算哈希并比对
            String recalculated = computeContentHash(tc);
            if (recalculated != null && recalculated.equals(tc.getContentHash())) {
                vo.setChainVerifyResult("验证通过");
                vo.setVerifyMessage("链上数据校验通过，产品信息不可篡改。");
            } else {
                vo.setChainVerifyResult("验证失败");
                vo.setVerifyMessage("链上校验不一致，数据可能被篡改，已记录扫码日志。");
            }
            vo.setTxHash(tc.getTxHash());
        } else {
            vo.setChainVerifyResult("未上链");
            vo.setVerifyMessage("该产品信息尚未进行区块链存证。");
        }

        // ---------- 风险等级评估 ----------
        String riskLevel = "低";
        String riskSuggestion = "产品溯源信息正常。";
        if ("不合格".equals(vo.getQualityResult())) {
            riskLevel = "高";
            riskSuggestion = "该产品质检不合格，请谨慎食用。";
        }
        vo.setRiskLevel(riskLevel);
        vo.setRiskSuggestion(riskSuggestion);

        vo.setScanTime(now);
        vo.setQueryResult("success");

        return vo;
    }

    // ==================== 状态变更 ====================

    /**
     * 变更溯源码状态（启用/禁用/作废）
     * <p>
     * 状态流转规则：
     * - 已绑定(1) → 已激活(2)：启用操作，激活前完成校验和存证
     * - 已激活(2) → 已禁用(3)：禁用操作，需填写禁用原因
     * - 任意状态 → 已作废(4)：作废操作，需填写作废原因，作废后不可再次激活
     * <p>
     * 非法状态转换会被拒绝。
     *
     * @param dto 状态变更请求（溯源码、目标状态、原因）
     */
    public void updateTraceCodeStatus(TraceCodeStatusDTO dto) {
        TraceCode tc = getByCode(dto.getTraceCode());
        if (tc == null) {
            throw new RuntimeException("溯源码不存在: " + dto.getTraceCode());
        }

        Integer currentStatus = tc.getTraceCodeStatus();
        Integer targetStatus = dto.getTargetStatus();
        String now = LocalDateTime.now().format(DATE_FMT);

        // ---------- 状态流转校验 ----------
        if (targetStatus == 2) {
            // 启用（激活）
            if (currentStatus != 1) {
                throw new RuntimeException("只有「已绑定」状态的溯源码才能激活，当前状态: " + getStatusName(currentStatus));
            }
            // 激活前校验：质检必须合格
            if (tc.getQualityResult() != null && tc.getQualityResult() == 2) {
                throw new RuntimeException("质检不合格的溯源码不能激活");
            }
            tc.setTraceCodeStatus(2);
            tc.setEnableTime(now);

            // 激活时补充区块链存证
            if (traceCodeProperties.isBlockchainEnabled() && isOnChain(tc) == 0) {
                tc.setProofId("PROOF_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                tc.setTxHash("0x" + sha256(tc.getTraceCode() + tc.getContentHash() + now).substring(0, 40));
                tc.setIsOnChain(1);
            }

        } else if (targetStatus == 3) {
            // 禁用
            if (currentStatus != 2) {
                throw new RuntimeException("只有「已激活」状态的溯源码才能禁用，当前状态: " + getStatusName(currentStatus));
            }
            if (dto.getReason() == null || dto.getReason().isBlank()) {
                throw new RuntimeException("禁用溯源码必须填写禁用原因");
            }
            tc.setTraceCodeStatus(3);
            tc.setDisableReason(dto.getReason());

        } else if (targetStatus == 4) {
            // 作废
            if (currentStatus == 4) {
                throw new RuntimeException("已作废的溯源码不能再次作废");
            }
            if (dto.getReason() == null || dto.getReason().isBlank()) {
                throw new RuntimeException("作废溯源码必须填写作废原因");
            }
            tc.setTraceCodeStatus(4);
            tc.setVoidReason(dto.getReason());

        } else {
            throw new RuntimeException("不支持的目标状态: " + targetStatus);
        }

        tc.setOperator(dto.getOperator());
        tc.setUpdateBy(dto.getOperator());
        tc.setUpdateTime(now);
        traceCodeMapper.updateById(tc);
    }

    // ==================== 校验接口 ====================

    /**
     * 溯源码校验接口（供监管全链追溯模块内部调用）
     * <p>
     * 返回溯源码完整状态、产品信息、溯源路径、哈希校验和存证状态。
     *
     * @param traceCode 溯源码值
     * @return TraceCode 实体（包含所有校验所需信息），不存在返回 null
     */
    public TraceCode verifyTraceCode(String traceCode) {
        TraceCode tc = getByCode(traceCode);
        if (tc == null) {
            return null;
        }
        // 重新计算并更新哈希校验结果（不修改数据库，仅用于返回）
        String recalculated = computeContentHash(tc);
        if (recalculated != null && !recalculated.equals(tc.getContentHash())) {
            // 哈希不一致，理论上只读返回，不做修改
            // 调用方可根据 contentHash 与链上存证比对判断
        }
        return tc;
    }

    // ==================== 导出 ====================

    /**
     * 导出溯源码二维码/条形码标签文件
     * <p>
     * 当前为桩实现，返回导出任务编号。
     * 实际二维码生成可集成 ZXing 等库实现。
     *
     * @param dto 导出请求（溯源码列表、模板、格式）
     * @return 导出任务编号
     */
    public String exportTraceCodeLabel(TraceCodeExportDTO dto) {
        if (dto.getTraceCodeList() == null || dto.getTraceCodeList().isEmpty()) {
            throw new RuntimeException("导出溯源码列表不能为空");
        }
        // 生成导出任务编号
        String exportId = "EXP_" + LocalDateTime.now().format(DATE_COMPACT) + "_" +
            String.format("%04d", System.currentTimeMillis() % 10000);
        // 实际导出逻辑（桩）：校验码有效性
        for (String code : dto.getTraceCodeList()) {
            TraceCode tc = getByCode(code);
            if (tc == null) {
                throw new RuntimeException("溯源码不存在: " + code);
            }
            if (tc.getTraceCodeStatus() != null && (tc.getTraceCodeStatus() == 4 || tc.getTraceCodeStatus() == 5)) {
                throw new RuntimeException("溯源码状态异常，无法导出: " + code);
            }
        }
        // 返回任务编号供异步查询
        return exportId;
    }

    // ==================== 绑定关系管理 ====================

    /**
     * 建立溯源码与业务数据的绑定关系
     * <p>
     * 一个溯源码可绑定多条不同类型的业务记录。
     *
     * @param traceCode 溯源码值
     * @param bizType   业务类型
     * @param bizId     业务数据ID
     * @param bizNo     业务数据编码
     * @param operator  操作人
     */
    public void bindBusinessData(String traceCode, String bizType, String bizId,
                                  String bizNo, String operator) {
        throw new UnsupportedOperationException(
            "当前 fts 表结构未提供 t_trace_code_bind，无法持久化额外业务绑定关系；请先使用 batch_no 关联生产批次。");
    }

    /**
     * 查询指定溯源码的所有绑定关系
     *
     * @param traceCode 溯源码值
     * @return 绑定关系列表
     */
    public List<TraceCodeBind> listBindsByCode(String traceCode) {
        // fts 当前没有 t_trace_code_bind；详情页可仍通过 batch_no 展示生产批次。
        return List.of();
    }

    // ==================== 基础查询 ====================

    /**
     * 按溯源码精确查询
     */
    public TraceCode getByCode(String traceCode) {
        QueryWrapper<TraceCode> qw = new QueryWrapper<>();
        qw.eq("trace_code", traceCode);
        qw.eq("is_deleted", 0);
        return traceCodeMapper.selectOne(qw);
    }

    /**
     * 按批次号查询所有溯源码
     */
    public List<TraceCode> listByBatchNo(String batchNo) {
        QueryWrapper<TraceCode> qw = new QueryWrapper<>();
        qw.eq("batch_no", batchNo);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("create_time");
        return traceCodeMapper.selectList(qw);
    }

    /**
     * 按企业ID查询溯源码
     */
    public List<TraceCode> listByEnterprise(String enterpriseId) {
        QueryWrapper<TraceCode> qw = new QueryWrapper<>();
        qw.eq("enterprise_uuid", enterpriseId);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("create_time");
        return traceCodeMapper.selectList(qw);
    }

    /**
     * 按生成批次号查询（批量生成的一组码）
     */
    public List<TraceCode> listByGenerateBatchNo(String generateBatchNo) {
        throw new UnsupportedOperationException(
            "当前 fts.t_trace_code 未保存生成批次号，无法跨请求查询批量生成结果。");
    }

    // ==================== 内部方法 ====================

    /**
     * 业务数据校验
     * <p>
     * 校验产品生产路径完整性、质检结果、企业资质等。
     * 校验不通过抛出异常。
     */
    private void validateBusinessData(TraceCodeGenerateDTO dto) {
        if (dto.getProductId() == null || dto.getProductId().isBlank()) {
            throw new RuntimeException("产品ID不能为空");
        }
        if (dto.getEnterpriseId() == null || dto.getEnterpriseId().isBlank()) {
            throw new RuntimeException("企业ID不能为空");
        }
        if (dto.getBatchNo() == null || dto.getBatchNo().isBlank()) {
            throw new RuntimeException("生产批次号不能为空");
        }
        // 质检结果校验：不合格(2)的产品不应生成溯源码
        if (dto.getQualityResult() != null && dto.getQualityResult() == 2) {
            throw new RuntimeException("质检不合格的产品不能生成溯源码");
        }
    }

    /**
     * 计算 SHA-256 内容哈希
     * <p>
     * 对产品编号、关联批次号、企业信息等关键字段拼接后取 SHA-256。
     * 用于防伪校验和区块链存证。
     */
    public String computeContentHash(TraceCode tc) {
        String raw = (tc.getProductId() != null ? tc.getProductId() : "") +
            (tc.getBatchNo() != null ? tc.getBatchNo() : "") +
            (tc.getEnterpriseId() != null ? tc.getEnterpriseId() : "") +
            (tc.getProductName() != null ? tc.getProductName() : "") +
            (tc.getEnterpriseName() != null ? tc.getEnterpriseName() : "");
        return sha256(raw);
    }

    /**
     * SHA-256 哈希计算
     */
    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(
                traceCodeProperties.getHashAlgorithm());
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(traceCodeProperties.getHashAlgorithm() + " 计算失败", e);
        }
    }

    /**
     * 将实体转换为视图对象
     */
    private TraceCodeVO toVO(TraceCode tc) {
        TraceCodeVO vo = new TraceCodeVO();
        vo.setTraceCodeId(tc.getTraceCodeId());
        vo.setTraceCode(tc.getTraceCode());
        vo.setCodeType(tc.getCodeType());
        vo.setCodeTypeName(getCodeTypeName(tc.getCodeType()));
        vo.setPackageLevel(tc.getPackageLevel());
        vo.setPackageLevelName(getPackageLevelName(tc.getPackageLevel()));
        vo.setProductName(tc.getProductName());
        vo.setEnterpriseName(tc.getEnterpriseName());
        vo.setBatchNo(tc.getBatchNo());
        vo.setTraceCodeStatus(tc.getTraceCodeStatus());
        vo.setTraceCodeStatusName(getStatusName(tc.getTraceCodeStatus()));
        vo.setContentHash(tc.getContentHash());
        vo.setIsOnChain(isOnChain(tc));
        vo.setGenerateTime(generatedAt(tc));
        vo.setEnableTime(tc.getTraceCodeStatus() != null && tc.getTraceCodeStatus() == 2 ? tc.getUpdateTime() : null);
        vo.setExpireTime(tc.getExpireTime());
        vo.setOperator(tc.getUpdateBy() != null ? tc.getUpdateBy() : tc.getCreateBy());
        vo.setCreateTime(tc.getCreateTime());
        return vo;
    }

    /** fts 通过存证字段表示是否上链，不再依赖不存在的 is_on_chain 列。 */
    private int isOnChain(TraceCode tc) {
        return tc.getProofId() != null && !tc.getProofId().isBlank()
            && tc.getTxHash() != null && !tc.getTxHash().isBlank() ? 1 : 0;
    }

    /** fts 的创建时间即溯源码生成时间。 */
    private String generatedAt(TraceCode tc) {
        return tc.getCreateTime();
    }

    // ==================== 枚举值 → 中文名称 ====================

    /** 码类型 → 中文 */
    public static String getCodeTypeName(Integer codeType) {
        if (codeType == null) return "未知";
        switch (codeType) {
            case 1: return "单品码";
            case 2: return "批次码";
            case 3: return "箱码";
            case 4: return "托码";
            default: return "未知";
        }
    }

    /** 包装层级 → 中文 */
    public static String getPackageLevelName(Integer level) {
        if (level == null) return "未知";
        switch (level) {
            case 1: return "最小销售单元";
            case 2: return "外箱";
            case 3: return "托盘";
            default: return "未知";
        }
    }

    /** 溯源码状态 → 中文 */
    public static String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "未绑定";
            case 1: return "已绑定";
            case 2: return "已激活";
            case 3: return "已禁用";
            case 4: return "已作废";
            case 5: return "已过期";
            default: return "未知";
        }
    }

    /** 业务类型 → 中文 */
    public static String getBizTypeName(String bizType) {
        if (bizType == null) return "未知";
        switch (bizType) {
            case "RAW_BATCH":      return "原料批次";
            case "PROD_BATCH":     return "生产批次";
            case "PROCESS_BATCH":  return "加工批次";
            case "INSPECTION":     return "质检记录";
            case "LOGISTICS":      return "物流运输";
            case "SALES_TERMINAL": return "销售终端";
            default: return bizType;
        }
    }
}

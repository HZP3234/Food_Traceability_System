package com.foodtraceability.enterprise;

import com.foodtraceability.enterprise.entity.Raw;
import com.foodtraceability.enterprise.service.RawService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
public class RawServiceSimpleTest {

    @Autowired
    private RawService rawService;

    @Test
    public void testCreateRaw() {
        // 创建原料批次对象 - 设置所有字段
        Raw raw = new Raw();

        // 基本信息
        raw.setSupplierName("王大山蔬菜合作社");
        raw.setSupplierId("SUP2026001");
        raw.setProductCategory("蔬菜");
        raw.setProductName("西红柿");
        raw.setAmount(new BigDecimal("500.50"));
        raw.setUnit("公斤");
        raw.setWarehouse("A1-03");
        raw.setStorageMethod(1);
        raw.setShelfLife("2026-07-23");
        raw.setPurchaseDate("2026-06-23");

        // 状态字段
        raw.setCheckResult(2);      // 2-待质检
        raw.setBatchStatus(1);      // 1-待入库
        raw.setDetailStatus(0);     // 0-未上传详情

        // 关联字段 - 设置默认值
        raw.setDetailId("0");
        raw.setDataHash("");

        // 操作人字段 - 新增这两个
        raw.setCreateBy("admin");   // 创建人
        raw.setUpdateBy("admin");   // 更新人

        // 其他字段
        raw.setIsDeleted(0);
        raw.setRemark("测试数据");

        // 调用新增方法
        int result = rawService.createRaw(raw);

        // 打印结果
        System.out.println("插入结果: " + result);
        System.out.println("生成的批次号: " + raw.getBatchNo());
        System.out.println("产品名称: " + raw.getProductName());
        System.out.println("数量: " + raw.getAmount() + " " + raw.getUnit());
        System.out.println("创建时间: " + raw.getCreateTime());
        System.out.println("创建人: " + raw.getCreateBy());

        // 验证
        assert result > 0 : "插入失败";
        assert raw.getBatchNo() != null : "批次号未生成";
    }
}
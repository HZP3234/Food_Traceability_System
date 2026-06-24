package com.foodtraceability.enterprise.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodtraceability.enterprise.entity.SalesTerminal;
import com.foodtraceability.enterprise.entity.SalesStock;
import com.foodtraceability.enterprise.entity.SalesStorage;
import com.foodtraceability.enterprise.entity.SalesSupplement;
import com.foodtraceability.enterprise.service.SalesService;

@RestController
@RequestMapping("/Sales")
public class SalesController {
    @Autowired
    private SalesService salesService;

    // ==================== t_sales_terminal ====================

    // 按终端编号查询
    @RequestMapping("/queryTerminal")
    public SalesTerminal queryTerminal(String terminalCode) {
        return salesService.getByTerminalCode(terminalCode);
    }

    // 条件列表查询
    @RequestMapping("/listTerminal")
    public List<SalesTerminal> listTerminal(Integer terminalType, String area, Integer terminalStatus) {
        return salesService.listTerminal(terminalType, area, terminalStatus);
    }

    // 按运营企业查询
    @RequestMapping("/listByOperator")
    public List<SalesTerminal> listByOperator(String operatorId) {
        return salesService.listByOperatorId(operatorId);
    }

    // 注册销售终端
    @RequestMapping("/createTerminal")
    public String createTerminal(SalesTerminal terminal) {
        int num = salesService.createTerminal(terminal);
        if (num == 1) {
            return "终端注册成功";
        } else {
            return "终端注册失败";
        }
    }

    // 更新终端信息
    @RequestMapping("/updateTerminal")
    public String updateTerminal(SalesTerminal terminal) {
        int num = salesService.updateTerminal(terminal);
        if (num == 1) {
            return "终端信息更新成功";
        } else {
            return "终端信息更新失败";
        }
    }

    // 软删除终端
    @RequestMapping("/deleteTerminal")
    public String deleteTerminal(Long terminalId) {
        int num = salesService.deleteTerminal(terminalId);
        if (num == 1) {
            return "终端删除成功";
        } else {
            return "终端删除失败";
        }
    }

    // ==================== t_sales_stock ====================

    // 按终端查询库存
    @RequestMapping("/listStock")
    public List<SalesStock> listStock(String terminalId) {
        return salesService.listStockByTerminalId(terminalId);
    }

    // 按生产批次查询库存分布
    @RequestMapping("/listStockByBatch")
    public List<SalesStock> listStockByBatch(String prodBatchNo) {
        return salesService.listStockByProdBatchNo(prodBatchNo);
    }

    // 产品入库
    @RequestMapping("/stockIn")
    public String stockIn(SalesStock stock) {
        int num = salesService.stockIn(stock);
        if (num == 1) {
            return "产品入库成功";
        } else {
            return "产品入库失败";
        }
    }

    // 更新库存盘点
    @RequestMapping("/updateStock")
    public String updateStock(SalesStock stock) {
        int num = salesService.updateStock(stock);
        if (num == 1) {
            return "库存盘点更新成功";
        } else {
            return "库存盘点更新失败";
        }
    }

    // ==================== t_sales_storage ====================

    // 查询储存环境
    @RequestMapping("/queryStorage")
    public SalesStorage queryStorage(String terminalCode) {
        return salesService.getStorageByTerminalCode(terminalCode);
    }

    // 手动更新储存环境
    @RequestMapping("/updateStorage")
    public String updateStorage(SalesStorage storage) {
        int num = salesService.updateStorage(storage);
        if (num == 1) {
            return "储存环境更新成功";
        } else {
            return "储存环境更新失败";
        }
    }

    // ==================== t_sales_supplement ====================

    // 按溯源码批次查询补充记录
    @RequestMapping("/listSupplement")
    public List<SalesSupplement> listSupplement(String traceBatchNo) {
        return salesService.listSupplementByTraceBatchNo(traceBatchNo);
    }

    // 按终端查询补充记录
    @RequestMapping("/listSupplementByTerminal")
    public List<SalesSupplement> listSupplementByTerminal(String terminalCode) {
        return salesService.listSupplementByTerminalCode(terminalCode);
    }

    // 销售商补充销售详情
    @RequestMapping("/supplementInfo")
    public String supplementInfo(SalesSupplement supplement) {
        int num = salesService.supplementSalesInfo(supplement);
        if (num == 1) {
            return "销售详情补充成功";
        } else {
            return "销售详情补充失败";
        }
    }

    // 更新补充信息
    @RequestMapping("/updateSupplement")
    public String updateSupplement(SalesSupplement supplement) {
        int num = salesService.updateSupplement(supplement);
        if (num == 1) {
            return "补充信息更新成功";
        } else {
            return "补充信息更新失败";
        }
    }

    // ==================== 防窜货核验 ====================

    // 按区域查询异常终端
    @RequestMapping("/antiFraudCheck")
    public List<SalesTerminal> antiFraudCheck(String area) {
        return salesService.checkAntiFraud(area);
    }

    // 执行全量防窜货核验
    @RequestMapping("/runAntiFraud")
    public String runAntiFraud() {
        salesService.runAntiFraudCheck();
        return "防窜货核验执行完毕";
    }
}

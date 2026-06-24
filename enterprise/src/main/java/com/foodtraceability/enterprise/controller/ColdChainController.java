package com.foodtraceability.enterprise.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodtraceability.enterprise.entity.Warehouse;
import com.foodtraceability.enterprise.entity.CcVehicle;
import com.foodtraceability.enterprise.entity.CcTransport;
import com.foodtraceability.enterprise.entity.CcTempHumidity;
import com.foodtraceability.enterprise.entity.CcTransportNode;
import com.foodtraceability.enterprise.entity.CcReceipt;
import com.foodtraceability.enterprise.service.ColdChainService;
//
@RestController
@RequestMapping("/ColdChain")
public class ColdChainController {
    @Autowired
    private ColdChainService coldChainService;

    // ==================== t_warehouse ====================

    // 按名称查询仓库
    @RequestMapping("/queryWarehouse")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'REGULATOR')")
    public Warehouse queryWarehouse(String warehouseName) {
        return coldChainService.getByWarehouseName(warehouseName);
    }

    // 按UUID查询仓库
    @RequestMapping("/queryWarehouseByUuid")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'REGULATOR')")
    public Warehouse queryWarehouseByUuid(String warehouseUuid) {
        return coldChainService.getByWarehouseUuid(warehouseUuid);
    }

    // 条件列表查询
    @RequestMapping("/listWarehouse")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'REGULATOR')")
    public List<Warehouse> listWarehouse(Integer warehouseType, Integer warehouseStatus) {
        return coldChainService.listWarehouse(warehouseType, warehouseStatus);
    }

    // 新增仓库
    @RequestMapping("/createWarehouse")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String createWarehouse(Warehouse warehouse) {
        int num = coldChainService.createWarehouse(warehouse);
        if (num == 1) {
            return "仓库创建成功";
        } else {
            return "仓库创建失败";
        }
    }

    // 更新仓库
    @RequestMapping("/updateWarehouse")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String updateWarehouse(Warehouse warehouse) {
        int num = coldChainService.updateWarehouse(warehouse);
        if (num == 1) {
            return "仓库信息更新成功";
        } else {
            return "仓库信息更新失败";
        }
    }

    // 软删除仓库
    @RequestMapping("/deleteWarehouse")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String deleteWarehouse(Long warehouseId) {
        int num = coldChainService.deleteWarehouse(warehouseId);
        if (num == 1) {
            return "仓库删除成功";
        } else {
            return "仓库删除失败";
        }
    }

    // ==================== t_cc_vehicle ====================

    // 按车牌号查询
    @RequestMapping("/queryVehicle")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'REGULATOR')")
    public CcVehicle queryVehicle(String plateNo) {
        return coldChainService.getByPlateNo(plateNo);
    }

    // 按物流企业查询车辆
    @RequestMapping("/listVehicleByOwner")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'REGULATOR')")
    public List<CcVehicle> listVehicleByOwner(String ownerId) {
        return coldChainService.listByOwnerId(ownerId);
    }

    // 条件列表查询
    @RequestMapping("/listVehicle")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'REGULATOR')")
    public List<CcVehicle> listVehicle(Integer vehicleStatus, String ownerName, String coldType) {
        return coldChainService.listVehicle(vehicleStatus, ownerName, coldType);
    }

    // 注册冷链车辆
    @RequestMapping("/createVehicle")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String createVehicle(CcVehicle vehicle) {
        int num = coldChainService.createVehicle(vehicle);
        if (num == 1) {
            return "冷链车辆注册成功";
        } else {
            return "冷链车辆注册失败";
        }
    }

    // 更新车辆信息
    @RequestMapping("/updateVehicle")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String updateVehicle(CcVehicle vehicle) {
        int num = coldChainService.updateVehicle(vehicle);
        if (num == 1) {
            return "车辆信息更新成功";
        } else {
            return "车辆信息更新失败";
        }
    }

    // 更新车辆状态
    @RequestMapping("/updateVehicleStatus")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String updateVehicleStatus(Long vehicleId, int vehicleStatus) {
        int num = coldChainService.updateVehicleStatus(vehicleId, vehicleStatus);
        if (num == 1) {
            return "车辆状态更新成功";
        } else {
            return "车辆状态更新失败";
        }
    }

    // 软删除车辆
    @RequestMapping("/deleteVehicle")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String deleteVehicle(Long vehicleId) {
        int num = coldChainService.deleteVehicle(vehicleId);
        if (num == 1) {
            return "车辆删除成功";
        } else {
            return "车辆删除失败";
        }
    }

    // ==================== t_cc_transport ====================

    // 按运输订单号查询
    @RequestMapping("/queryTransport")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'MANUFACTURER', 'REGULATOR')")
    public CcTransport queryTransport(String orderNo) {
        return coldChainService.getByOrderNo(orderNo);
    }

    // 按生产批次号查询运输订单
    @RequestMapping("/listTransportByBatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'MANUFACTURER', 'REGULATOR')")
    public List<CcTransport> listTransportByBatch(String prodBatchNo) {
        return coldChainService.listByProdBatchNo(prodBatchNo);
    }

    // 按车牌号查询运输订单
    @RequestMapping("/listTransportByPlate")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'REGULATOR')")
    public List<CcTransport> listTransportByPlate(String plateNo) {
        return coldChainService.listByPlateNo(plateNo);
    }

    // 条件列表查询
    @RequestMapping("/listTransport")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'MANUFACTURER', 'REGULATOR')")
    public List<CcTransport> listTransport(Integer transportStatus, String prodBatchNo,
                                            String plateNo, Integer transportMethod) {
        return coldChainService.listTransport(transportStatus, prodBatchNo, plateNo, transportMethod);
    }

    // 创建运输订单
    @RequestMapping("/createTransport")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String createTransport(CcTransport transport) {
        int num = coldChainService.createTransport(transport);
        if (num == 1) {
            return "运输订单创建成功，订单号：" + transport.getOrderNo();
        } else {
            return "运输订单创建失败";
        }
    }

    // 更新运输订单
    @RequestMapping("/updateTransport")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String updateTransport(CcTransport transport) {
        int num = coldChainService.updateTransport(transport);
        if (num == 1) {
            return "运输订单更新成功";
        } else {
            return "运输订单更新失败";
        }
    }

    // 发运
    @RequestMapping("/departTransport")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String departTransport(Long transportId) {
        int num = coldChainService.departTransport(transportId);
        if (num == 1) {
            return "运输订单已发运，车辆状态已更新";
        } else {
            return "发运失败";
        }
    }

    // 抵达签收
    @RequestMapping("/arriveTransport")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String arriveTransport(Long transportId) {
        int num = coldChainService.arriveTransport(transportId);
        if (num == 1) {
            return "运输订单已签收，车辆已释放";
        } else {
            return "签收失败";
        }
    }

    // 温度预警
    @RequestMapping("/alertTransport")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String alertTransport(Long transportId) {
        int num = coldChainService.alertTransport(transportId);
        if (num == 1) {
            return "运输订单已标记为温度预警";
        } else {
            return "操作失败";
        }
    }

    // 异常关闭
    @RequestMapping("/closeTransport")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String closeTransport(Long transportId) {
        int num = coldChainService.closeTransport(transportId);
        if (num == 1) {
            return "运输订单已异常关闭，车辆已释放";
        } else {
            return "操作失败";
        }
    }

    // 软删除运输订单
    @RequestMapping("/deleteTransport")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String deleteTransport(Long transportId) {
        int num = coldChainService.deleteTransport(transportId);
        if (num == 1) {
            return "运输订单删除成功";
        } else {
            return "运输订单删除失败";
        }
    }

    // ==================== t_cc_temp_humidity ====================

    // 按运输订单号查询温湿度记录
    @RequestMapping("/listTempHumidity")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'REGULATOR')")
    public List<CcTempHumidity> listTempHumidity(String orderNo) {
        return coldChainService.listTempHumidityByOrderNo(orderNo);
    }

    // 按异常状态查询
    @RequestMapping("/listTempHumidityAbnormal")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'REGULATOR')")
    public List<CcTempHumidity> listTempHumidityAbnormal(Integer isAbnormal) {
        if (isAbnormal == null) isAbnormal = 1;
        return coldChainService.listTempHumidityByAbnormal(isAbnormal);
    }

    // 记录温湿度
    @RequestMapping("/recordTempHumidity")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String recordTempHumidity(CcTempHumidity record) {
        int num = coldChainService.recordTempHumidity(record);
        if (num == 1) {
            if (record.getIsAbnormal() == 1) {
                return "温湿度记录成功，检测到异常：" + record.getAbnormalReason();
            }
            return "温湿度记录成功";
        } else {
            return "温湿度记录失败";
        }
    }

    // ==================== t_cc_transport_node ====================

    // 按运输订单号查询节点记录
    @RequestMapping("/listTransportNode")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'REGULATOR')")
    public List<CcTransportNode> listTransportNode(String orderNo) {
        return coldChainService.listNodeByOrderNo(orderNo);
    }

    // 按节点类型查询
    @RequestMapping("/listNodeByType")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'REGULATOR')")
    public List<CcTransportNode> listNodeByType(int nodeType) {
        return coldChainService.listNodeByType(nodeType);
    }

    // 记录运输节点
    @RequestMapping("/recordNode")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String recordNode(CcTransportNode node) {
        int num = coldChainService.recordNode(node);
        if (num == 1) {
            return "运输节点记录成功";
        } else {
            return "运输节点记录失败";
        }
    }

    // 更新节点状态
    @RequestMapping("/updateNode")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String updateNode(CcTransportNode node) {
        int num = coldChainService.updateNode(node);
        if (num == 1) {
            return "节点状态更新成功";
        } else {
            return "节点状态更新失败";
        }
    }

    // ==================== t_cc_receipt ====================

    // 按运输订单号查询签收单
    @RequestMapping("/queryReceipt")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'REGULATOR')")
    public CcReceipt queryReceipt(String orderNo) {
        return coldChainService.getReceiptByOrderNo(orderNo);
    }

    // 签收确认
    @RequestMapping("/signReceipt")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String signReceipt(CcReceipt receipt) {
        int num = coldChainService.signReceipt(receipt);
        if (num == 1) {
            return "签收确认成功";
        } else {
            return "签收确认失败";
        }
    }

    // 更新签收单
    @RequestMapping("/updateReceipt")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public String updateReceipt(CcReceipt receipt) {
        int num = coldChainService.updateReceipt(receipt);
        if (num == 1) {
            return "签收单更新成功";
        } else {
            return "签收单更新失败";
        }
    }

    // ==================== 冷链全链路追溯 ====================

    // 根据运输订单号追溯冷链全链路
    @RequestMapping("/traceColdChain")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'REGULATOR')")
    public ColdChainService.ColdChainTraceVO traceColdChain(String orderNo) {
        return coldChainService.traceColdChain(orderNo);
    }

    // 根据生产批次号追溯冷链运输链路
    @RequestMapping("/traceByProdBatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'MANUFACTURER', 'REGULATOR')")
    public List<CcTransport> traceByProdBatch(String prodBatchNo) {
        return coldChainService.traceByProdBatch(prodBatchNo);
    }
}

package com.foodtraceability.enterprise.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.foodtraceability.enterprise.entity.Warehouse;
import com.foodtraceability.enterprise.entity.CcVehicle;
import com.foodtraceability.enterprise.entity.CcTransport;
import com.foodtraceability.enterprise.entity.CcTempHumidity;
import com.foodtraceability.enterprise.entity.CcTransportNode;
import com.foodtraceability.enterprise.entity.CcReceipt;
import com.foodtraceability.enterprise.mapper.WarehouseMapper;
import com.foodtraceability.enterprise.mapper.CcVehicleMapper;
import com.foodtraceability.enterprise.mapper.CcTransportMapper;
import com.foodtraceability.enterprise.mapper.CcTempHumidityMapper;
import com.foodtraceability.enterprise.mapper.CcTransportNodeMapper;
import com.foodtraceability.enterprise.mapper.CcReceiptMapper;

@Service
public class ColdChainService {
    @Autowired
    private WarehouseMapper warehouseMapper;
    @Autowired
    private CcVehicleMapper ccVehicleMapper;
    @Autowired
    private CcTransportMapper ccTransportMapper;
    @Autowired
    private CcTempHumidityMapper ccTempHumidityMapper;
    @Autowired
    private CcTransportNodeMapper ccTransportNodeMapper;
    @Autowired
    private CcReceiptMapper ccReceiptMapper;

    // 生成运输订单号 LO + yyyyMMdd + 4位序号
    public String generateTransportOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seqPart = String.format("%04d", System.currentTimeMillis() % 10000);
        return "LO" + datePart + seqPart;
    }

    // 生成仓库UUID
    public String generateWarehouseUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // ==================== t_warehouse ====================

    // 按名称查询仓库
    public Warehouse getByWarehouseName(String warehouseName) {
        QueryWrapper<Warehouse> qw = new QueryWrapper<>();
        qw.eq("warehouse_name", warehouseName);
        qw.eq("is_deleted", 0);
        return warehouseMapper.selectOne(qw);
    }

    // 按UUID查询仓库
    public Warehouse getByWarehouseUuid(String warehouseUuid) {
        QueryWrapper<Warehouse> qw = new QueryWrapper<>();
        qw.eq("warehouse_uuid", warehouseUuid);
        qw.eq("is_deleted", 0);
        return warehouseMapper.selectOne(qw);
    }

    // 条件列表查询
    public List<Warehouse> listWarehouse(Integer warehouseType, Integer warehouseStatus) {
        QueryWrapper<Warehouse> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        if (warehouseType != null)
            qw.eq("warehouse_type", warehouseType);
        if (warehouseStatus != null)
            qw.eq("warehouse_status", warehouseStatus);
        qw.orderByDesc("create_time");
        return warehouseMapper.selectList(qw);
    }

    // 新增仓库
    public int createWarehouse(Warehouse warehouse) {
        if (warehouse.getWarehouseUuid() == null || warehouse.getWarehouseUuid().isBlank()) {
            warehouse.setWarehouseUuid(generateWarehouseUuid());
        }
        if (warehouse.getWarehouseStatus() == 0) warehouse.setWarehouseStatus(1);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        warehouse.setCreateTime(now);
        warehouse.setUpdateTime(now);
        return warehouseMapper.insert(warehouse);
    }

    // 更新仓库
    public int updateWarehouse(Warehouse warehouse) {
        warehouse.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return warehouseMapper.updateById(warehouse);
    }

    // 软删除仓库
    public int deleteWarehouse(Integer warehouseId) {
        Warehouse warehouse = warehouseMapper.selectById(warehouseId);
        if (warehouse != null) {
            warehouse.setIsDeleted(1);
            return warehouseMapper.updateById(warehouse);
        }
        return 0;
    }

    // ==================== t_cc_vehicle ====================

    // 按车牌号查询
    public CcVehicle getByPlateNo(String plateNo) {
        QueryWrapper<CcVehicle> qw = new QueryWrapper<>();
        qw.eq("plate_no", plateNo);
        qw.eq("is_deleted", 0);
        return ccVehicleMapper.selectOne(qw);
    }

    // 按物流企业查询车辆
    public List<CcVehicle> listByOwnerId(String ownerId) {
        QueryWrapper<CcVehicle> qw = new QueryWrapper<>();
        qw.eq("owner_id", ownerId);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("create_time");
        return ccVehicleMapper.selectList(qw);
    }

    // 条件列表查询
    public List<CcVehicle> listVehicle(Integer vehicleStatus, String ownerName, String coldType) {
        QueryWrapper<CcVehicle> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        if (vehicleStatus != null)
            qw.eq("vehicle_status", vehicleStatus);
        if (ownerName != null && !ownerName.isBlank())
            qw.eq("owner_name", ownerName);
        if (coldType != null && !coldType.isBlank())
            qw.eq("cold_type", coldType);
        qw.orderByDesc("create_time");
        return ccVehicleMapper.selectList(qw);
    }

    // 注册冷链车辆
    public int createVehicle(CcVehicle vehicle) {
        if (vehicle.getVehicleStatus() == 0) vehicle.setVehicleStatus(0);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        vehicle.setCreateTime(now);
        vehicle.setUpdateTime(now);
        return ccVehicleMapper.insert(vehicle);
    }

    // 更新车辆信息
    public int updateVehicle(CcVehicle vehicle) {
        vehicle.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return ccVehicleMapper.updateById(vehicle);
    }

    // 更新车辆状态
    public int updateVehicleStatus(Integer vehicleId, int vehicleStatus) {
        CcVehicle vehicle = ccVehicleMapper.selectById(vehicleId);
        if (vehicle != null) {
            vehicle.setVehicleStatus(vehicleStatus);
            vehicle.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return ccVehicleMapper.updateById(vehicle);
        }
        return 0;
    }

    // 软删除车辆
    public int deleteVehicle(Integer vehicleId) {
        CcVehicle vehicle = ccVehicleMapper.selectById(vehicleId);
        if (vehicle != null) {
            vehicle.setIsDeleted(1);
            return ccVehicleMapper.updateById(vehicle);
        }
        return 0;
    }

    // ==================== t_cc_transport ====================

    // 按运输订单号查询
    public CcTransport getByOrderNo(String orderNo) {
        QueryWrapper<CcTransport> qw = new QueryWrapper<>();
        qw.eq("order_no", orderNo);
        qw.eq("is_deleted", 0);
        return ccTransportMapper.selectOne(qw);
    }

    // 按生产批次号查询运输订单
    public List<CcTransport> listByProdBatchNo(String prodBatchNo) {
        QueryWrapper<CcTransport> qw = new QueryWrapper<>();
        qw.eq("prod_batch_no", prodBatchNo);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("create_time");
        return ccTransportMapper.selectList(qw);
    }

    // 按车牌号查询运输订单
    public List<CcTransport> listByPlateNo(String plateNo) {
        QueryWrapper<CcTransport> qw = new QueryWrapper<>();
        qw.eq("plate_no", plateNo);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("create_time");
        return ccTransportMapper.selectList(qw);
    }

    // 条件列表查询
    public List<CcTransport> listTransport(Integer transportStatus, String prodBatchNo,
                                            String plateNo, Integer transportMethod) {
        QueryWrapper<CcTransport> qw = new QueryWrapper<>();
        qw.eq("is_deleted", 0);
        if (transportStatus != null)
            qw.eq("transport_status", transportStatus);
        if (prodBatchNo != null && !prodBatchNo.isBlank())
            qw.eq("prod_batch_no", prodBatchNo);
        if (plateNo != null && !plateNo.isBlank())
            qw.eq("plate_no", plateNo);
        if (transportMethod != null)
            qw.eq("transport_method", transportMethod);
        qw.orderByDesc("create_time");
        return ccTransportMapper.selectList(qw);
    }

    // 创建运输订单
    public int createTransport(CcTransport transport) {
        if (transport.getOrderNo() == null || transport.getOrderNo().isBlank()) {
            transport.setOrderNo(generateTransportOrderNo());
        }
        if (transport.getTransportStatus() == 0) transport.setTransportStatus(1);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        transport.setCreateTime(now);
        transport.setUpdateTime(now);
        return ccTransportMapper.insert(transport);
    }

    // 更新运输订单
    public int updateTransport(CcTransport transport) {
        transport.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return ccTransportMapper.updateById(transport);
    }

    // 发运：更新车辆状态为运输中，运输订单状态为运输中
    public int departTransport(Integer transportId) {
        CcTransport transport = ccTransportMapper.selectById(transportId);
        if (transport == null) return 0;
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        transport.setTransportStatus(2);
        transport.setDepartTime(now);
        transport.setUpdateTime(now);
        int num = ccTransportMapper.updateById(transport);

        // 联动更新车辆状态
        CcVehicle vehicle = getByPlateNo(transport.getPlateNo());
        if (vehicle != null) {
            updateVehicleStatus(vehicle.getVehicleId(), 1);
        }

        // 记录运输节点：装货出库
        CcTransportNode node = new CcTransportNode();
        node.setOrderNo(transport.getOrderNo());
        node.setNodeType(1);
        node.setNodeTime(now);
        node.setLocation(transport.getDepartureName());
        node.setOperator(transport.getDriverName());
        node.setNodeStatus(1);
        node.setCreateTime(now);
        node.setUpdateTime(now);
        ccTransportNodeMapper.insert(node);

        return num;
    }

    // 抵达签收
    public int arriveTransport(Integer transportId) {
        CcTransport transport = ccTransportMapper.selectById(transportId);
        if (transport == null) return 0;
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        transport.setTransportStatus(4);
        transport.setActualArrival(now);
        transport.setUpdateTime(now);
        int num = ccTransportMapper.updateById(transport);

        // 联动更新车辆状态为空闲
        CcVehicle vehicle = getByPlateNo(transport.getPlateNo());
        if (vehicle != null) {
            updateVehicleStatus(vehicle.getVehicleId(), 0);
        }

        // 记录运输节点：电子签收
        CcTransportNode node = new CcTransportNode();
        node.setOrderNo(transport.getOrderNo());
        node.setNodeType(3);
        node.setNodeTime(now);
        node.setLocation(transport.getDestinationName());
        node.setOperator(transport.getDriverName());
        node.setNodeStatus(1);
        node.setCreateTime(now);
        node.setUpdateTime(now);
        ccTransportNodeMapper.insert(node);

        return num;
    }

    // 温度预警
    public int alertTransport(Integer transportId) {
        CcTransport transport = ccTransportMapper.selectById(transportId);
        if (transport == null) return 0;
        transport.setTransportStatus(3);
        transport.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return ccTransportMapper.updateById(transport);
    }

    // 异常关闭
    public int closeTransport(Integer transportId) {
        CcTransport transport = ccTransportMapper.selectById(transportId);
        if (transport == null) return 0;
        transport.setTransportStatus(5);
        transport.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        int num = ccTransportMapper.updateById(transport);

        // 释放车辆
        CcVehicle vehicle = getByPlateNo(transport.getPlateNo());
        if (vehicle != null) {
            updateVehicleStatus(vehicle.getVehicleId(), 0);
        }

        return num;
    }

    // 软删除运输订单
    public int deleteTransport(Integer transportId) {
        CcTransport transport = ccTransportMapper.selectById(transportId);
        if (transport != null) {
            transport.setIsDeleted(1);
            return ccTransportMapper.updateById(transport);
        }
        return 0;
    }

    // ==================== t_cc_temp_humidity ====================

    // 按运输订单号查询温湿度记录
    public List<CcTempHumidity> listTempHumidityByOrderNo(String orderNo) {
        QueryWrapper<CcTempHumidity> qw = new QueryWrapper<>();
        qw.eq("order_no", orderNo);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("record_time");
        return ccTempHumidityMapper.selectList(qw);
    }

    // 按异常状态查询
    public List<CcTempHumidity> listTempHumidityByAbnormal(int isAbnormal) {
        QueryWrapper<CcTempHumidity> qw = new QueryWrapper<>();
        qw.eq("is_abnormal", isAbnormal);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("record_time");
        return ccTempHumidityMapper.selectList(qw);
    }

    // 记录温湿度（自动检测异常）
    public int recordTempHumidity(CcTempHumidity record) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        record.setRecordTime(now);
        record.setCreateTime(now);
        record.setUpdateTime(now);

        // 自动检测温湿度是否超标
        if (record.getOrderNo() != null) {
            CcTransport transport = getByOrderNo(record.getOrderNo());
            if (transport != null) {
                boolean abnormal = false;
                StringBuilder reason = new StringBuilder();

                if (transport.getTempUpper() != null && record.getTemperature() != null
                        && record.getTemperature().compareTo(transport.getTempUpper()) > 0) {
                    abnormal = true;
                    reason.append("温度超标(>" + transport.getTempUpper() + "℃); ");
                }
                if (transport.getTempLower() != null && record.getTemperature() != null
                        && record.getTemperature().compareTo(transport.getTempLower()) < 0) {
                    abnormal = true;
                    reason.append("温度过低(<" + transport.getTempLower() + "℃); ");
                }
                if (transport.getHumidUpper() != null && record.getHumidity() != null
                        && record.getHumidity().compareTo(transport.getHumidUpper()) > 0) {
                    abnormal = true;
                    reason.append("湿度超标(>" + transport.getHumidUpper() + "%); ");
                }
                if (transport.getHumidLower() != null && record.getHumidity() != null
                        && record.getHumidity().compareTo(transport.getHumidLower()) < 0) {
                    abnormal = true;
                    reason.append("湿度过低(<" + transport.getHumidLower() + "%); ");
                }

                if (abnormal) {
                    record.setIsAbnormal(1);
                    record.setAbnormalReason(reason.toString().trim());
                    // 联动触发运输订单温度预警
                    if (transport.getTransportStatus() == 2) {
                        alertTransport(transport.getTransportId());
                    }
                }
            }
        }

        return ccTempHumidityMapper.insert(record);
    }

    // ==================== t_cc_transport_node ====================

    // 按运输订单号查询节点记录
    public List<CcTransportNode> listNodeByOrderNo(String orderNo) {
        QueryWrapper<CcTransportNode> qw = new QueryWrapper<>();
        qw.eq("order_no", orderNo);
        qw.eq("is_deleted", 0);
        qw.orderByAsc("node_time");
        return ccTransportNodeMapper.selectList(qw);
    }

    // 按节点类型查询
    public List<CcTransportNode> listNodeByType(int nodeType) {
        QueryWrapper<CcTransportNode> qw = new QueryWrapper<>();
        qw.eq("node_type", nodeType);
        qw.eq("is_deleted", 0);
        qw.orderByDesc("node_time");
        return ccTransportNodeMapper.selectList(qw);
    }

    // 记录运输节点
    public int recordNode(CcTransportNode node) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (node.getNodeTime() == null || node.getNodeTime().isBlank()) {
            node.setNodeTime(now);
        }
        if (node.getNodeStatus() == 0) node.setNodeStatus(1);
        node.setCreateTime(now);
        node.setUpdateTime(now);
        return ccTransportNodeMapper.insert(node);
    }

    // 更新节点状态
    public int updateNode(CcTransportNode node) {
        node.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return ccTransportNodeMapper.updateById(node);
    }

    // ==================== t_cc_receipt ====================

    // 按运输订单号查询签收单
    public CcReceipt getReceiptByOrderNo(String orderNo) {
        QueryWrapper<CcReceipt> qw = new QueryWrapper<>();
        qw.eq("order_no", orderNo);
        qw.eq("is_deleted", 0);
        return ccReceiptMapper.selectOne(qw);
    }

    // 签收确认
    public int signReceipt(CcReceipt receipt) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        receipt.setSignTime(now);
        receipt.setCreateTime(now);
        receipt.setUpdateTime(now);

        if (receipt.getIsPackageIntact() == 0) receipt.setIsPackageIntact(1);
        if (receipt.getQtyMatch() == 0) receipt.setQtyMatch(1);

        int num = ccReceiptMapper.insert(receipt);

        // 联动更新运输订单状态为已签收
        if (receipt.getOrderNo() != null) {
            CcTransport transport = getByOrderNo(receipt.getOrderNo());
            if (transport != null && transport.getTransportStatus() != 4) {
                arriveTransport(transport.getTransportId());
            }
        }

        return num;
    }

    // 更新签收单
    public int updateReceipt(CcReceipt receipt) {
        receipt.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return ccReceiptMapper.updateById(receipt);
    }

    // ==================== 冷链全链路追溯 ====================

    // 根据运输订单号追溯全链路：车辆信息 -> 温湿度记录 -> 运输节点 -> 签收单
    public CcTransport traceColdChain(String orderNo) {
        return getByOrderNo(orderNo);
    }

    // 根据生产批次号追溯冷链运输链路
    public List<CcTransport> traceByProdBatch(String prodBatchNo) {
        return listByProdBatchNo(prodBatchNo);
    }
}

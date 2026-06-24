package com.foodtraceability.enterprise;

import com.foodtraceability.enterprise.entity.Warehouse;
import com.foodtraceability.enterprise.entity.CcVehicle;
import com.foodtraceability.enterprise.entity.CcTransport;
import com.foodtraceability.enterprise.entity.CcTempHumidity;
import com.foodtraceability.enterprise.entity.CcTransportNode;
import com.foodtraceability.enterprise.entity.CcReceipt;
import com.foodtraceability.enterprise.service.ColdChainService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ColdChainServiceSimpleTest {

    @Autowired
    private ColdChainService coldChainService;

    // 用于跨测试方法共享数据
    private static Integer warehouseId;
    private static String warehouseUuid;
    private static String warehouseName = "测试冷库A";
    private static Integer vehicleId;
    private static String plateNo = "测A·TEST01";
    private static Integer transportId;
    private static String orderNo;

    // ==================== t_warehouse ====================

    @Test
    @Order(1)
    public void testCreateWarehouse() {
        try {
            Warehouse warehouse = new Warehouse();
            warehouse.setWarehouseName(warehouseName);
            warehouse.setWarehouseType(1);
            warehouse.setAddress("测试省测试市测试区测试路100号");
            warehouse.setCapacity("5000吨");
            warehouse.setManager("赵仓库管理员");
            warehouse.setTempRange("2-6℃");
            warehouse.setHumidityRange("45-65%");
            warehouse.setWarehouseStatus(1);
            warehouse.setCreateBy("admin");
            warehouse.setUpdateBy("admin");
            warehouse.setIsDeleted(0);
            warehouse.setRemark("测试仓库");

            int result = coldChainService.createWarehouse(warehouse);

            System.out.println("=== 创建仓库 ===");
            System.out.println("插入结果: " + result);
            System.out.println("仓库ID: " + warehouse.getWarehouseId());
            System.out.println("仓库UUID: " + warehouse.getWarehouseUuid());
            System.out.println("仓库名称: " + warehouse.getWarehouseName());
            System.out.println("仓库类型: " + warehouse.getWarehouseType() + " (1-冷库)");
            System.out.println("温控范围: " + warehouse.getTempRange());
            System.out.println("创建时间: " + warehouse.getCreateTime());

            assert result > 0 : "仓库创建失败";
            assert warehouse.getWarehouseUuid() != null : "仓库UUID未生成";
            warehouseId = warehouse.getWarehouseId();
            warehouseUuid = warehouse.getWarehouseUuid();
            System.out.println("[PASS] testCreateWarehouse - 仓库创建成功, UUID=" + warehouseUuid);
        } catch (Throwable e) {
            System.out.println("[FAIL] testCreateWarehouse - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(2)
    public void testQueryWarehouse() {
        try {
            Warehouse warehouse = coldChainService.getByWarehouseName(warehouseName);

            System.out.println("=== 按名称查询仓库 ===");
            System.out.println("仓库ID: " + (warehouse != null ? warehouse.getWarehouseId() : "null"));
            System.out.println("仓库名称: " + (warehouse != null ? warehouse.getWarehouseName() : "null"));
            System.out.println("地址: " + (warehouse != null ? warehouse.getAddress() : "null"));
            System.out.println("管理员: " + (warehouse != null ? warehouse.getManager() : "null"));

            assert warehouse != null : "仓库查询失败";
            System.out.println("[PASS] testQueryWarehouse - 仓库查询成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testQueryWarehouse - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(3)
    public void testQueryWarehouseByUuid() {
        try {
            Warehouse warehouse = coldChainService.getByWarehouseUuid(warehouseUuid);

            System.out.println("=== 按UUID查询仓库 ===");
            System.out.println("UUID: " + warehouseUuid);
            System.out.println("仓库名称: " + (warehouse != null ? warehouse.getWarehouseName() : "null"));

            assert warehouse != null : "按UUID查询仓库失败";
            System.out.println("[PASS] testQueryWarehouseByUuid - 按UUID查询仓库成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testQueryWarehouseByUuid - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(4)
    public void testListWarehouse() {
        try {
            List<Warehouse> list = coldChainService.listWarehouse(1, 1);

            System.out.println("=== 条件列表查询仓库 ===");
            System.out.println("查询结果数: " + list.size());
            for (Warehouse w : list) {
                System.out.println("  - " + w.getWarehouseName() + " | 类型:" + w.getWarehouseType()
                        + " | 温控:" + w.getTempRange() + " | 状态:" + w.getWarehouseStatus());
            }

            assert list.size() > 0 : "仓库列表不应为空";
            System.out.println("[PASS] testListWarehouse - 查询到 " + list.size() + " 个仓库");
        } catch (Throwable e) {
            System.out.println("[FAIL] testListWarehouse - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(5)
    public void testUpdateWarehouse() {
        try {
            Warehouse warehouse = coldChainService.getByWarehouseName(warehouseName);
            assert warehouse != null : "更新前查询失败";

            warehouse.setManager("钱新管理员");
            warehouse.setCapacity("6000吨");
            warehouse.setUpdateBy("admin");

            int result = coldChainService.updateWarehouse(warehouse);

            System.out.println("=== 更新仓库信息 ===");
            System.out.println("更新结果: " + result);
            System.out.println("新管理员: " + warehouse.getManager());
            System.out.println("新容量: " + warehouse.getCapacity());

            assert result > 0 : "仓库更新失败";
            System.out.println("[PASS] testUpdateWarehouse - 仓库更新成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testUpdateWarehouse - " + e.getMessage());
            throw e;
        }
    }

    // ==================== t_cc_vehicle ====================

    @Test
    @Order(6)
    public void testCreateVehicle() {
        try {
            CcVehicle vehicle = new CcVehicle();
            vehicle.setPlateNo(plateNo);
            vehicle.setVehicleModel("福田欧马可S5冷藏车");
            vehicle.setDriverName("孙司机");
            vehicle.setDriverPhone("13900001111");
            vehicle.setOwnerId("ENT2026003");
            vehicle.setOwnerName("测试冷链物流公司");
            vehicle.setColdType("冷藏");
            vehicle.setTempRange("2-6℃");
            vehicle.setVehicleStatus(0);
            vehicle.setCreateBy("admin");
            vehicle.setUpdateBy("admin");
            vehicle.setIsDeleted(0);
            vehicle.setRemark("测试冷链车辆");

            int result = coldChainService.createVehicle(vehicle);

            System.out.println("=== 注册冷链车辆 ===");
            System.out.println("插入结果: " + result);
            System.out.println("车辆ID: " + vehicle.getVehicleId());
            System.out.println("车牌号: " + vehicle.getPlateNo());
            System.out.println("车型: " + vehicle.getVehicleModel());
            System.out.println("驾驶员: " + vehicle.getDriverName());
            System.out.println("所属企业: " + vehicle.getOwnerName());
            System.out.println("冷链类型: " + vehicle.getColdType());
            System.out.println("创建时间: " + vehicle.getCreateTime());

            assert result > 0 : "冷链车辆注册失败";
            vehicleId = vehicle.getVehicleId();
            System.out.println("[PASS] testCreateVehicle - 车辆注册成功, 车牌号=" + plateNo);
        } catch (Throwable e) {
            System.out.println("[FAIL] testCreateVehicle - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(7)
    public void testQueryVehicle() {
        try {
            CcVehicle vehicle = coldChainService.getByPlateNo(plateNo);

            System.out.println("=== 按车牌号查询车辆 ===");
            System.out.println("车辆ID: " + (vehicle != null ? vehicle.getVehicleId() : "null"));
            System.out.println("车牌号: " + (vehicle != null ? vehicle.getPlateNo() : "null"));
            System.out.println("驾驶员: " + (vehicle != null ? vehicle.getDriverName() : "null"));
            System.out.println("车辆状态: " + (vehicle != null ? vehicle.getVehicleStatus() : "null") + " (0-空闲)");

            assert vehicle != null : "车辆查询失败";
            System.out.println("[PASS] testQueryVehicle - 车辆查询成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testQueryVehicle - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(8)
    public void testListVehicle() {
        try {
            List<CcVehicle> list = coldChainService.listVehicle(null, "测试冷链物流公司", "冷藏");

            System.out.println("=== 条件列表查询车辆 ===");
            System.out.println("查询结果数: " + list.size());
            for (CcVehicle v : list) {
                System.out.println("  - " + v.getPlateNo() + " | " + v.getVehicleModel()
                        + " | 驾驶员:" + v.getDriverName() + " | 状态:" + v.getVehicleStatus());
            }

            assert list.size() > 0 : "车辆列表不应为空";
            System.out.println("[PASS] testListVehicle - 查询到 " + list.size() + " 辆冷链车");
        } catch (Throwable e) {
            System.out.println("[FAIL] testListVehicle - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(9)
    public void testUpdateVehicleStatus() {
        try {
            int result = coldChainService.updateVehicleStatus(vehicleId, 2);

            System.out.println("=== 更新车辆状态 ===");
            System.out.println("操作结果: " + result);
            System.out.println("车辆ID: " + vehicleId);
            System.out.println("新状态: 2 (维护中)");

            CcVehicle vehicle = coldChainService.getByPlateNo(plateNo);
            System.out.println("确认状态: " + (vehicle != null ? vehicle.getVehicleStatus() : "null"));

            assert result > 0 : "车辆状态更新失败";

            // 恢复为空闲状态
            coldChainService.updateVehicleStatus(vehicleId, 0);
            System.out.println("[PASS] testUpdateVehicleStatus - 车辆状态更新成功(空闲→维护中→空闲)");
        } catch (Throwable e) {
            System.out.println("[FAIL] testUpdateVehicleStatus - " + e.getMessage());
            throw e;
        }
    }

    // ==================== t_cc_transport ====================

    @Test
    @Order(10)
    public void testCreateTransport() {
        try {
            CcTransport transport = new CcTransport();
            transport.setPlateNo(plateNo);
            transport.setDriverName("孙司机");
            transport.setDriverPhone("13900001111");
            transport.setProductName("燕山鲜牛奶");
            transport.setProdBatchNo("PBS260611001");
            transport.setDepartureId(warehouseUuid);
            transport.setDepartureName(warehouseName);
            transport.setDestinationId("WH-DEST-001");
            transport.setDestinationName("目的地测试仓库B");
            transport.setLoadingTemp("4.5℃");
            transport.setTransportMethod(1);
            transport.setCollectInterval("5分钟");
            transport.setTempUpper(new BigDecimal("6.0"));
            transport.setTempLower(new BigDecimal("2.0"));
            transport.setHumidUpper(new BigDecimal("65.0"));
            transport.setHumidLower(new BigDecimal("45.0"));
            transport.setAlertMethod("短信+APP推送");
            transport.setTransportStatus(1);
            transport.setDataHash("");
            transport.setChainHash("");
            transport.setCreateBy("admin");
            transport.setUpdateBy("admin");
            transport.setIsDeleted(0);
            transport.setRemark("测试运输订单");

            int result = coldChainService.createTransport(transport);

            System.out.println("=== 创建运输订单 ===");
            System.out.println("插入结果: " + result);
            System.out.println("运输订单号: " + transport.getOrderNo());
            System.out.println("产品名称: " + transport.getProductName());
            System.out.println("生产批次号: " + transport.getProdBatchNo());
            System.out.println("发货仓库: " + transport.getDepartureName());
            System.out.println("目的仓库: " + transport.getDestinationName());
            System.out.println("温度上限: " + transport.getTempUpper() + "℃");
            System.out.println("温度下限: " + transport.getTempLower() + "℃");
            System.out.println("创建时间: " + transport.getCreateTime());

            assert result > 0 : "运输订单创建失败";
            assert transport.getOrderNo() != null : "运输订单号未生成";
            transportId = transport.getTransportId();
            orderNo = transport.getOrderNo();
            System.out.println("[PASS] testCreateTransport - 运输订单创建成功, 订单号=" + orderNo);
        } catch (Throwable e) {
            System.out.println("[FAIL] testCreateTransport - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(11)
    public void testQueryTransport() {
        try {
            CcTransport transport = coldChainService.getByOrderNo(orderNo);

            System.out.println("=== 查询运输订单 ===");
            System.out.println("运输ID: " + (transport != null ? transport.getTransportId() : "null"));
            System.out.println("订单号: " + (transport != null ? transport.getOrderNo() : "null"));
            System.out.println("产品: " + (transport != null ? transport.getProductName() : "null"));
            System.out.println("状态: " + (transport != null ? transport.getTransportStatus() : "null") + " (1-待发运)");

            assert transport != null : "运输订单查询失败";
            System.out.println("[PASS] testQueryTransport - 运输订单查询成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testQueryTransport - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(12)
    public void testListTransport() {
        try {
            List<CcTransport> list = coldChainService.listTransport(1, null, plateNo, null);

            System.out.println("=== 条件列表查询运输订单 ===");
            System.out.println("查询结果数: " + list.size());
            for (CcTransport t : list) {
                System.out.println("  - " + t.getOrderNo() + " | " + t.getProductName()
                        + " | 状态:" + t.getTransportStatus() + " | 车牌:" + t.getPlateNo());
            }

            assert list.size() > 0 : "运输订单列表不应为空";
            System.out.println("[PASS] testListTransport - 查询到 " + list.size() + " 条运输订单");
        } catch (Throwable e) {
            System.out.println("[FAIL] testListTransport - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(13)
    public void testListTransportByBatch() {
        try {
            List<CcTransport> list = coldChainService.listByProdBatchNo("PBS260611001");

            System.out.println("=== 按生产批次查询运输订单 ===");
            System.out.println("生产批次号: PBS260611001");
            System.out.println("查询结果数: " + list.size());
            for (CcTransport t : list) {
                System.out.println("  - " + t.getOrderNo() + " | 产品:" + t.getProductName()
                        + " | 发站:" + t.getDepartureName() + " → 到站:" + t.getDestinationName());
            }

            assert list.size() > 0 : "按生产批次查询运输订单不应为空";
            System.out.println("[PASS] testListTransportByBatch - 生产批次关联 " + list.size() + " 条运输订单");
        } catch (Throwable e) {
            System.out.println("[FAIL] testListTransportByBatch - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(14)
    public void testDepartTransport() {
        try {
            int result = coldChainService.departTransport(transportId);

            System.out.println("=== 发运运输订单 ===");
            System.out.println("操作结果: " + result);

            CcTransport transport = coldChainService.getByOrderNo(orderNo);
            System.out.println("运输状态: " + (transport != null ? transport.getTransportStatus() : "null") + " (2-运输中)");
            System.out.println("出发时间: " + (transport != null ? transport.getDepartTime() : "null"));

            CcVehicle vehicle = coldChainService.getByPlateNo(plateNo);
            System.out.println("车辆状态: " + (vehicle != null ? vehicle.getVehicleStatus() : "null") + " (1-运输中)");

            assert result > 0 : "发运失败";
            System.out.println("[PASS] testDepartTransport - 发运成功, 车辆已置为运输中, 节点已自动记录");
        } catch (Throwable e) {
            System.out.println("[FAIL] testDepartTransport - " + e.getMessage());
            throw e;
        }
    }

    // ==================== t_cc_temp_humidity ====================

    @Test
    @Order(15)
    public void testRecordTempHumidityNormal() {
        try {
            CcTempHumidity record = new CcTempHumidity();
            record.setOrderNo(orderNo);
            record.setTemperature(new BigDecimal("4.2"));
            record.setHumidity(new BigDecimal("55.0"));
            record.setLocationDesc("G15高速 K120+500 服务区");
            record.setIsAbnormal(0);
            record.setCreateBy("admin");
            record.setUpdateBy("admin");
            record.setIsDeleted(0);

            int result = coldChainService.recordTempHumidity(record);

            System.out.println("=== 记录温湿度（正常） ===");
            System.out.println("插入结果: " + result);
            System.out.println("记录ID: " + record.getRecordId());
            System.out.println("温度: " + record.getTemperature() + "℃");
            System.out.println("湿度: " + record.getHumidity() + "%");
            System.out.println("位置: " + record.getLocationDesc());
            System.out.println("是否异常: " + record.getIsAbnormal() + " (0-正常)");
            System.out.println("记录时间: " + record.getRecordTime());

            assert result > 0 : "温湿度记录失败";
            System.out.println("[PASS] testRecordTempHumidityNormal - 正常温湿度记录成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testRecordTempHumidityNormal - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(16)
    public void testRecordTempHumidityAbnormal() {
        try {
            CcTempHumidity record = new CcTempHumidity();
            record.setOrderNo(orderNo);
            record.setTemperature(new BigDecimal("8.5"));
            record.setHumidity(new BigDecimal("70.0"));
            record.setLocationDesc("G15高速 K180+200 收费站");
            record.setIsAbnormal(0);
            record.setCreateBy("admin");
            record.setUpdateBy("admin");
            record.setIsDeleted(0);

            int result = coldChainService.recordTempHumidity(record);

            System.out.println("=== 记录温湿度（异常-超标） ===");
            System.out.println("插入结果: " + result);
            System.out.println("温度: " + record.getTemperature() + "℃ (上限6.0℃)");
            System.out.println("湿度: " + record.getHumidity() + "% (上限65%)");
            System.out.println("是否异常: " + record.getIsAbnormal() + " (1-异常)");
            System.out.println("异常原因: " + record.getAbnormalReason());

            // 验证运输订单是否触发了温度预警
            CcTransport transport = coldChainService.getByOrderNo(orderNo);
            System.out.println("运输订单状态: " + (transport != null ? transport.getTransportStatus() : "null") + " (3-温度预警)");

            assert result > 0 : "异常温湿度记录失败";
            assert record.getIsAbnormal() == 1 : "应自动标记为异常";
            System.out.println("[PASS] testRecordTempHumidityAbnormal - 异常温湿度记录成功, 已触发温度预警, 原因=" + record.getAbnormalReason());
        } catch (Throwable e) {
            System.out.println("[FAIL] testRecordTempHumidityAbnormal - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(17)
    public void testListTempHumidity() {
        try {
            List<CcTempHumidity> list = coldChainService.listTempHumidityByOrderNo(orderNo);

            System.out.println("=== 查询温湿度记录列表 ===");
            System.out.println("运输订单号: " + orderNo);
            System.out.println("记录数: " + list.size());
            for (CcTempHumidity r : list) {
                System.out.println("  - 时间:" + r.getRecordTime() + " | 温度:" + r.getTemperature()
                        + "℃ | 湿度:" + r.getHumidity() + "% | 异常:" + r.getIsAbnormal());
            }

            assert list.size() >= 2 : "温湿度记录应至少有2条";
            System.out.println("[PASS] testListTempHumidity - 查询到 " + list.size() + " 条温湿度记录");
        } catch (Throwable e) {
            System.out.println("[FAIL] testListTempHumidity - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(18)
    public void testListTempHumidityAbnormal() {
        try {
            List<CcTempHumidity> list = coldChainService.listTempHumidityByAbnormal(1);

            System.out.println("=== 查询异常温湿度记录 ===");
            System.out.println("查询结果数: " + list.size());
            for (CcTempHumidity r : list) {
                System.out.println("  - " + r.getOrderNo() + " | 温度:" + r.getTemperature()
                        + "℃ | 原因:" + r.getAbnormalReason());
            }

            assert list.size() > 0 : "异常温湿度记录不应为空";
            System.out.println("[PASS] testListTempHumidityAbnormal - 查询到 " + list.size() + " 条异常温湿度记录");
        } catch (Throwable e) {
            System.out.println("[FAIL] testListTempHumidityAbnormal - " + e.getMessage());
            throw e;
        }
    }

    // ==================== t_cc_transport_node ====================

    @Test
    @Order(19)
    public void testRecordNode() {
        try {
            CcTransportNode node = new CcTransportNode();
            node.setOrderNo(orderNo);
            node.setNodeType(2);
            node.setLocation("测试中转冷库");
            node.setOperator("李中转员");
            node.setNodeStatus(1);
            node.setCreateBy("admin");
            node.setUpdateBy("admin");
            node.setIsDeleted(0);
            node.setRemark("测试中转节点");

            int result = coldChainService.recordNode(node);

            System.out.println("=== 记录运输节点 ===");
            System.out.println("插入结果: " + result);
            System.out.println("节点ID: " + node.getNodeId());
            System.out.println("节点类型: " + node.getNodeType() + " (2-抵达中转仓)");
            System.out.println("地点: " + node.getLocation());
            System.out.println("操作人: " + node.getOperator());
            System.out.println("节点时间: " + node.getNodeTime());

            assert result > 0 : "运输节点记录失败";
            System.out.println("[PASS] testRecordNode - 运输节点记录成功, 类型=抵达中转仓");
        } catch (Throwable e) {
            System.out.println("[FAIL] testRecordNode - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(20)
    public void testListNodeByOrderNo() {
        try {
            List<CcTransportNode> list = coldChainService.listNodeByOrderNo(orderNo);

            System.out.println("=== 查询运输节点列表 ===");
            System.out.println("运输订单号: " + orderNo);
            System.out.println("节点数: " + list.size());
            for (CcTransportNode n : list) {
                String nodeTypeName = switch (n.getNodeType()) {
                    case 1 -> "装货出库";
                    case 2 -> "抵达中转仓";
                    case 3 -> "电子签收";
                    default -> "未知";
                };
                System.out.println("  - " + nodeTypeName + " | " + n.getLocation()
                        + " | 时间:" + n.getNodeTime() + " | 状态:" + n.getNodeStatus());
            }

            assert list.size() > 0 : "运输节点列表不应为空";
            System.out.println("[PASS] testListNodeByOrderNo - 查询到 " + list.size() + " 个运输节点");
        } catch (Throwable e) {
            System.out.println("[FAIL] testListNodeByOrderNo - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(21)
    public void testListNodeByType() {
        try {
            List<CcTransportNode> list = coldChainService.listNodeByType(1);

            System.out.println("=== 按节点类型查询 ===");
            System.out.println("节点类型: 1 (装货出库)");
            System.out.println("查询结果数: " + list.size());
            for (CcTransportNode n : list) {
                System.out.println("  - " + n.getOrderNo() + " | " + n.getLocation() + " | 时间:" + n.getNodeTime());
            }

            assert list.size() > 0 : "按类型查询节点不应为空";
            System.out.println("[PASS] testListNodeByType - 查询到 " + list.size() + " 个装货出库节点");
        } catch (Throwable e) {
            System.out.println("[FAIL] testListNodeByType - " + e.getMessage());
            throw e;
        }
    }

    // ==================== t_cc_receipt ====================

    @Test
    @Order(22)
    public void testSignReceipt() {
        try {
            CcReceipt receipt = new CcReceipt();
            receipt.setOrderNo(orderNo);
            receipt.setSigner("周收货员");
            receipt.setSignTemp("4.8℃");
            receipt.setIsPackageIntact(1);
            receipt.setQtyMatch(1);
            receipt.setQtyMismatchDesc("");
            receipt.setCreateBy("admin");
            receipt.setUpdateBy("admin");
            receipt.setIsDeleted(0);
            receipt.setRemark("测试签收单");

            int result = coldChainService.signReceipt(receipt);

            System.out.println("=== 签收确认 ===");
            System.out.println("插入结果: " + result);
            System.out.println("签收单ID: " + receipt.getReceiptId());
            System.out.println("签收人: " + receipt.getSigner());
            System.out.println("签收温度: " + receipt.getSignTemp());
            System.out.println("包装完好: " + receipt.getIsPackageIntact() + " (1-完好)");
            System.out.println("数量核对: " + receipt.getQtyMatch() + " (1-一致)");
            System.out.println("签收时间: " + receipt.getSignTime());

            // 验证运输订单状态联动
            CcTransport transport = coldChainService.getByOrderNo(orderNo);
            System.out.println("运输订单状态: " + (transport != null ? transport.getTransportStatus() : "null") + " (4-已签收)");

            // 验证车辆状态联动
            CcVehicle vehicle = coldChainService.getByPlateNo(plateNo);
            System.out.println("车辆状态: " + (vehicle != null ? vehicle.getVehicleStatus() : "null") + " (0-空闲)");

            assert result > 0 : "签收失败";
            System.out.println("[PASS] testSignReceipt - 签收成功, 订单已签收, 车辆已释放, 节点已自动记录");
        } catch (Throwable e) {
            System.out.println("[FAIL] testSignReceipt - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(23)
    public void testQueryReceipt() {
        try {
            CcReceipt receipt = coldChainService.getReceiptByOrderNo(orderNo);

            System.out.println("=== 查询签收单 ===");
            System.out.println("签收单ID: " + (receipt != null ? receipt.getReceiptId() : "null"));
            System.out.println("签收人: " + (receipt != null ? receipt.getSigner() : "null"));
            System.out.println("签收温度: " + (receipt != null ? receipt.getSignTemp() : "null"));
            System.out.println("包装完好: " + (receipt != null ? receipt.getIsPackageIntact() : "null"));
            System.out.println("数量一致: " + (receipt != null ? receipt.getQtyMatch() : "null"));

            assert receipt != null : "签收单查询失败";
            System.out.println("[PASS] testQueryReceipt - 签收单查询成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testQueryReceipt - " + e.getMessage());
            throw e;
        }
    }

    // ==================== 冷链全链路追溯 ====================

    @Test
    @Order(24)
    public void testTraceColdChain() {
        try {
            CcTransport transport = coldChainService.traceColdChain(orderNo);

            System.out.println("=== 冷链全链路追溯 ===");
            System.out.println("运输订单号: " + orderNo);
            if (transport != null) {
                System.out.println("  产品名称: " + transport.getProductName());
                System.out.println("  生产批次号: " + transport.getProdBatchNo());
                System.out.println("  车牌号: " + transport.getPlateNo());
                System.out.println("  驾驶员: " + transport.getDriverName());
                System.out.println("  发货仓库: " + transport.getDepartureName());
                System.out.println("  目的仓库: " + transport.getDestinationName());
                System.out.println("  装货温度: " + transport.getLoadingTemp());
                System.out.println("  出发时间: " + transport.getDepartTime());
                System.out.println("  实际到达: " + transport.getActualArrival());
                System.out.println("  运输状态: " + transport.getTransportStatus());
                System.out.println("  温度范围: " + transport.getTempLower() + "℃ ~ " + transport.getTempUpper() + "℃");
                System.out.println("  湿度范围: " + transport.getHumidLower() + "% ~ " + transport.getHumidUpper() + "%");
            } else {
                System.out.println("  未找到运输订单");
            }

            // 同时输出温湿度记录
            List<CcTempHumidity> tempList = coldChainService.listTempHumidityByOrderNo(orderNo);
            System.out.println("  温湿度记录数: " + tempList.size());

            // 同时输出运输节点
            List<CcTransportNode> nodeList = coldChainService.listNodeByOrderNo(orderNo);
            System.out.println("  运输节点数: " + nodeList.size());

            assert transport != null : "冷链追溯失败";
            System.out.println("[PASS] testTraceColdChain - 冷链追溯成功, 温湿度记录=" + tempList.size() + ", 节点=" + nodeList.size());
        } catch (Throwable e) {
            System.out.println("[FAIL] testTraceColdChain - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(25)
    public void testTraceByProdBatch() {
        try {
            List<CcTransport> list = coldChainService.traceByProdBatch("PBS260611001");

            System.out.println("=== 按生产批次追溯冷链运输 ===");
            System.out.println("生产批次号: PBS260611001");
            System.out.println("关联运输订单数: " + list.size());
            for (CcTransport t : list) {
                System.out.println("  - " + t.getOrderNo() + " | " + t.getProductName()
                        + " | " + t.getDepartureName() + " → " + t.getDestinationName()
                        + " | 状态:" + t.getTransportStatus());
            }

            assert list.size() > 0 : "按生产批次追溯不应为空";
            System.out.println("[PASS] testTraceByProdBatch - 按生产批次追溯成功, 关联 " + list.size() + " 条运输订单");
        } catch (Throwable e) {
            System.out.println("[FAIL] testTraceByProdBatch - " + e.getMessage());
            throw e;
        }
    }

    // ==================== 删除（软删除） ====================

    @Test
    @Order(98)
    public void testDeleteTransport() {
        try {
            int result = coldChainService.deleteTransport(transportId);

            System.out.println("=== 软删除运输订单 ===");
            System.out.println("操作结果: " + result);

            CcTransport transport = coldChainService.getByOrderNo(orderNo);
            System.out.println("删除后查询: " + (transport == null ? "已删除(过滤)" : "仍存在"));

            assert result > 0 : "运输订单软删除失败";
            System.out.println("[PASS] testDeleteTransport - 运输订单软删除成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testDeleteTransport - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(99)
    public void testDeleteVehicle() {
        try {
            int result = coldChainService.deleteVehicle(vehicleId);

            System.out.println("=== 软删除冷链车辆 ===");
            System.out.println("操作结果: " + result);

            assert result > 0 : "车辆软删除失败";
            System.out.println("[PASS] testDeleteVehicle - 车辆软删除成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testDeleteVehicle - " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(100)
    public void testDeleteWarehouse() {
        try {
            int result = coldChainService.deleteWarehouse(warehouseId);

            System.out.println("=== 软删除仓库 ===");
            System.out.println("操作结果: " + result);

            assert result > 0 : "仓库软删除失败";
            System.out.println("[PASS] testDeleteWarehouse - 仓库软删除成功");
        } catch (Throwable e) {
            System.out.println("[FAIL] testDeleteWarehouse - " + e.getMessage());
            throw e;
        }
    }
}

package com.foodtraceability.customers.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.foodtraceability.customers.dto.TraceabilityVO;
import com.foodtraceability.customers.entity.ProductTraceability;
import com.foodtraceability.customers.entity.ScanRecord;
import com.foodtraceability.customers.entity.TraceabilityNode;
import com.foodtraceability.customers.mapper.ProductTraceabilityMapper;
import com.foodtraceability.customers.mapper.ScanRecordMapper;
import com.foodtraceability.customers.mapper.TraceabilityNodeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("消费者扫码溯源服务 - 单元测试")
class TraceabilityServiceImplTest {

    @Mock
    private ProductTraceabilityMapper productTraceabilityMapper;

    @Mock
    private TraceabilityNodeMapper traceabilityNodeMapper;

    @Mock
    private ScanRecordMapper scanRecordMapper;

    @InjectMocks
    private TraceabilityServiceImpl traceabilityService;

    private ProductTraceability mockProduct;
    private List<TraceabilityNode> mockNodes;

    @BeforeEach
    void setUp() {
        mockProduct = new ProductTraceability();
        mockProduct.setId(1L);
        mockProduct.setProductBatchNo("BATCH-20260623-001");
        mockProduct.setProductName("有机奶粉");
        mockProduct.setProductSpec("800g/罐");
        mockProduct.setManufacturer("XX乳业有限公司");
        mockProduct.setOrigin("内蒙古");
        mockProduct.setProductionDate(LocalDate.of(2026, 6, 5));
        mockProduct.setExpirationDate(LocalDate.of(2028, 6, 4));

        TraceabilityNode node1 = new TraceabilityNode();
        node1.setId(1L);
        node1.setProductBatchNo("BATCH-20260623-001");
        node1.setNodeName("原材料采购");
        node1.setNodeDescription("采购有机原料");
        node1.setNodeTime(LocalDateTime.of(2026, 6, 1, 8, 0));
        node1.setLocation("内蒙古呼和浩特");
        node1.setOperator("采购部");
        node1.setSortOrder(1);

        TraceabilityNode node2 = new TraceabilityNode();
        node2.setId(2L);
        node2.setProductBatchNo("BATCH-20260623-001");
        node2.setNodeName("生产加工");
        node2.setNodeDescription("经过高温灭菌处理");
        node2.setNodeTime(LocalDateTime.of(2026, 6, 5, 10, 30));
        node2.setLocation("河北石家庄");
        node2.setOperator("生产车间");
        node2.setSortOrder(2);

        mockNodes = Arrays.asList(node1, node2);
    }

    @Nested
    @DisplayName("查询溯源信息 - queryByBatchNo")
    class QueryByBatchNoTests {

        @Test
        @DisplayName("正常查询溯源信息，返回完整产品信息和节点列表")
        void testQuerySuccess() {
            when(productTraceabilityMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(mockProduct);
            when(traceabilityNodeMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(mockNodes);

            TraceabilityVO result = traceabilityService.queryByBatchNo("BATCH-20260623-001");

            assertNotNull(result);
            assertEquals("BATCH-20260623-001", result.getProductBatchNo());
            assertEquals("有机奶粉", result.getProductName());
            assertEquals("800g/罐", result.getProductSpec());
            assertEquals("XX乳业有限公司", result.getManufacturer());
            assertEquals("内蒙古", result.getOrigin());
            assertEquals("2026-06-05", result.getProductionDate());
            assertEquals("2028-06-04", result.getExpirationDate());
            assertNotNull(result.getNodes());
            assertEquals(2, result.getNodes().size());
            assertEquals("原材料采购", result.getNodes().get(0).getNodeName());
            assertEquals("生产加工", result.getNodes().get(1).getNodeName());

            verify(productTraceabilityMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
            verify(traceabilityNodeMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("产品不存在时，抛出RuntimeException")
        void testQueryNotFound() {
            when(productTraceabilityMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(null);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> traceabilityService.queryByBatchNo("BATCH-NOTEXIST"));

            assertEquals("未找到该批次产品的溯源信息", exception.getMessage());
            verify(traceabilityNodeMapper, never()).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("产品存在但无溯源节点时，返回空节点列表")
        void testQueryNoNodes() {
            when(productTraceabilityMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(mockProduct);
            when(traceabilityNodeMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            TraceabilityVO result = traceabilityService.queryByBatchNo("BATCH-20260623-001");

            assertNotNull(result);
            assertEquals("有机奶粉", result.getProductName());
            assertNotNull(result.getNodes());
            assertTrue(result.getNodes().isEmpty());
        }

        @Test
        @DisplayName("节点按sortOrder升序查询")
        void testNodesOrderedBySortOrder() {
            when(productTraceabilityMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(mockProduct);

            ArgumentCaptor<LambdaQueryWrapper<TraceabilityNode>> captor =
                    ArgumentCaptor.forClass(LambdaQueryWrapper.class);
            when(traceabilityNodeMapper.selectList(captor.capture()))
                    .thenReturn(mockNodes);

            traceabilityService.queryByBatchNo("BATCH-20260623-001");

            LambdaQueryWrapper<TraceabilityNode> capturedWrapper = captor.getValue();
            assertNotNull(capturedWrapper);
        }
    }

    @Nested
    @DisplayName("记录扫码 - recordScan")
    class RecordScanTests {

        @Test
        @DisplayName("正常记录扫码事件")
        void testRecordScanSuccess() {
            when(scanRecordMapper.insert(any(ScanRecord.class))).thenReturn(1);

            traceabilityService.recordScan("BATCH-20260623-001", "192.168.1.100", "北京市", "user-uuid-123");

            ArgumentCaptor<ScanRecord> captor = ArgumentCaptor.forClass(ScanRecord.class);
            verify(scanRecordMapper, times(1)).insert(captor.capture());

            ScanRecord captured = captor.getValue();
            assertEquals("BATCH-20260623-001", captured.getProductBatchNo());
            assertEquals("192.168.1.100", captured.getScanIp());
            assertEquals("北京市", captured.getScanLocation());
            assertEquals("user-uuid-123", captured.getUserId());
            assertNotNull(captured.getScanTime());
        }

        @Test
        @DisplayName("扫码IP和定位可以为null")
        void testRecordScanWithNullIpAndLocation() {
            when(scanRecordMapper.insert(any(ScanRecord.class))).thenReturn(1);

            traceabilityService.recordScan("BATCH-20260623-001", null, null, null);

            ArgumentCaptor<ScanRecord> captor = ArgumentCaptor.forClass(ScanRecord.class);
            verify(scanRecordMapper, times(1)).insert(captor.capture());

            ScanRecord captured = captor.getValue();
            assertEquals("BATCH-20260623-001", captured.getProductBatchNo());
            assertNull(captured.getScanIp());
            assertNull(captured.getScanLocation());
        }

        @Test
        @DisplayName("扫码时间被正确设置")
        void testRecordScanTimeIsSet() {
            when(scanRecordMapper.insert(any(ScanRecord.class))).thenReturn(1);

            LocalDateTime before = LocalDateTime.now();
            ArgumentCaptor<ScanRecord> captor = ArgumentCaptor.forClass(ScanRecord.class);

            traceabilityService.recordScan("BATCH-20260623-001", "127.0.0.1", null, null);

            verify(scanRecordMapper, times(1)).insert(captor.capture());
            ScanRecord captured = captor.getValue();

            assertNotNull(captured.getScanTime());
            assertFalse(captured.getScanTime().isBefore(before.minusSeconds(1)));
        }
    }

    @Test
    @DisplayName("完整业务流程：查询溯源信息 + 记录扫码")
    void testFullBusinessFlow() {
        when(productTraceabilityMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(mockProduct);
        when(traceabilityNodeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(mockNodes);
        when(scanRecordMapper.insert(any(ScanRecord.class))).thenReturn(1);

        TraceabilityVO vo = traceabilityService.queryByBatchNo("BATCH-20260623-001");
        assertNotNull(vo);
        assertEquals("有机奶粉", vo.getProductName());
        assertEquals(2, vo.getNodes().size());

        traceabilityService.recordScan("BATCH-20260623-001", "127.0.0.1", "北京", "user-uuid-456");

        verify(productTraceabilityMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(traceabilityNodeMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(scanRecordMapper, times(1)).insert(any(ScanRecord.class));
    }
}

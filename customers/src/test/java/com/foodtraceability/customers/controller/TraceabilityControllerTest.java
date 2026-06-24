package com.foodtraceability.customers.controller;

import com.foodtraceability.customers.config.CustomersGlobalExceptionHandler;
import com.foodtraceability.customers.dto.ScanRecordDTO;
import com.foodtraceability.customers.dto.TraceabilityQueryDTO;
import com.foodtraceability.customers.dto.TraceabilityVO;
import com.foodtraceability.customers.entity.TraceabilityNode;
import com.foodtraceability.customers.service.TraceabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("消费者扫码溯源控制器 - 接口测试")
class TraceabilityControllerTest {

    @Mock
    private TraceabilityService traceabilityService;

    @InjectMocks
    private TraceabilityController traceabilityController;

    private MockMvc mockMvc;

    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private TraceabilityVO mockVO;

    @BeforeEach
    void setUp() {
        objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(traceabilityController)
                .setControllerAdvice(new CustomersGlobalExceptionHandler())
                .build();

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

        mockVO = new TraceabilityVO();
        mockVO.setProductBatchNo("BATCH-20260623-001");
        mockVO.setProductName("有机奶粉");
        mockVO.setProductSpec("800g/罐");
        mockVO.setManufacturer("XX乳业有限公司");
        mockVO.setOrigin("内蒙古");
        mockVO.setProductionDate(LocalDate.of(2026, 6, 5));
        mockVO.setExpirationDate(LocalDate.of(2028, 6, 4));
        mockVO.setNodes(Arrays.asList(node1, node2));
    }

    @Nested
    @DisplayName("POST /api/traceability/query - 扫码查询溯源")
    class QueryTests {

        @Test
        @DisplayName("正常查询溯源信息，返回200和完整溯源数据")
        void testQuerySuccess() throws Exception {
            when(traceabilityService.queryByBatchNo("BATCH-20260623-001"))
                    .thenReturn(mockVO);
            doNothing().when(traceabilityService)
                    .recordScan(anyString(), anyString(), isNull());

            TraceabilityQueryDTO dto = new TraceabilityQueryDTO();
            dto.setProductBatchNo("BATCH-20260623-001");

            mockMvc.perform(post("/api/traceability/query")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.productBatchNo").value("BATCH-20260623-001"))
                    .andExpect(jsonPath("$.data.productName").value("有机奶粉"))
                    .andExpect(jsonPath("$.data.productSpec").value("800g/罐"))
                    .andExpect(jsonPath("$.data.manufacturer").value("XX乳业有限公司"))
                    .andExpect(jsonPath("$.data.origin").value("内蒙古"))
                    .andExpect(jsonPath("$.data.nodes.length()").value(2))
                    .andExpect(jsonPath("$.data.nodes[0].nodeName").value("原材料采购"))
                    .andExpect(jsonPath("$.data.nodes[1].nodeName").value("生产加工"));

            verify(traceabilityService, times(1)).queryByBatchNo("BATCH-20260623-001");
            verify(traceabilityService, times(1))
                    .recordScan(eq("BATCH-20260623-001"), anyString(), isNull());
        }

        @Test
        @DisplayName("商品无溯源信息时返回500错误")
        void testQueryNotFound() throws Exception {
            when(traceabilityService.queryByBatchNo("BATCH-NOTEXIST"))
                    .thenThrow(new RuntimeException("未找到该批次产品的溯源信息"));

            TraceabilityQueryDTO dto = new TraceabilityQueryDTO();
            dto.setProductBatchNo("BATCH-NOTEXIST");

            mockMvc.perform(post("/api/traceability/query")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("未找到该批次产品的溯源信息"));

            verify(traceabilityService, never()).recordScan(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("批次号为空时返回400错误")
        void testQueryMissingBatchNo() throws Exception {
            TraceabilityQueryDTO dto = new TraceabilityQueryDTO();

            mockMvc.perform(post("/api/traceability/query")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message", containsString("产品批次号不能为空")));

            verify(traceabilityService, never()).queryByBatchNo(anyString());
        }

        @Test
        @DisplayName("溯源节点按sortOrder升序排列")
        void testNodesAreSortedByOrder() throws Exception {
            when(traceabilityService.queryByBatchNo("BATCH-20260623-001"))
                    .thenReturn(mockVO);
            doNothing().when(traceabilityService)
                    .recordScan(anyString(), anyString(), isNull());

            TraceabilityQueryDTO dto = new TraceabilityQueryDTO();
            dto.setProductBatchNo("BATCH-20260623-001");

            mockMvc.perform(post("/api/traceability/query")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(jsonPath("$.data.nodes[0].sortOrder").value(1))
                    .andExpect(jsonPath("$.data.nodes[1].sortOrder").value(2));
        }
    }

    @Nested
    @DisplayName("POST /api/traceability/scan - 记录扫码事件")
    class ScanTests {

        @Test
        @DisplayName("正常记录扫码事件，返回200")
        void testScanSuccess() throws Exception {
            doNothing().when(traceabilityService)
                    .recordScan(anyString(), anyString(), isNull());

            ScanRecordDTO dto = new ScanRecordDTO();
            dto.setProductBatchNo("BATCH-20260623-001");

            mockMvc.perform(post("/api/traceability/scan")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"));

            verify(traceabilityService, times(1))
                    .recordScan(eq("BATCH-20260623-001"), anyString(), isNull());
        }

        @Test
        @DisplayName("带扫码IP和定位信息记录")
        void testScanWithIpAndLocation() throws Exception {
            doNothing().when(traceabilityService)
                    .recordScan(anyString(), anyString(), anyString());

            ScanRecordDTO dto = new ScanRecordDTO();
            dto.setProductBatchNo("BATCH-20260623-001");
            dto.setScanIp("192.168.1.100");
            dto.setScanLocation("北京市朝阳区");

            mockMvc.perform(post("/api/traceability/scan")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(traceabilityService, times(1))
                    .recordScan("BATCH-20260623-001", "192.168.1.100", "北京市朝阳区");
        }

        @Test
        @DisplayName("缺少批次号时返回400错误")
        void testScanMissingBatchNo() throws Exception {
            ScanRecordDTO dto = new ScanRecordDTO();

            mockMvc.perform(post("/api/traceability/scan")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message", containsString("产品批次号不能为空")));

            verify(traceabilityService, never()).recordScan(anyString(), anyString(), anyString());
        }
    }
}

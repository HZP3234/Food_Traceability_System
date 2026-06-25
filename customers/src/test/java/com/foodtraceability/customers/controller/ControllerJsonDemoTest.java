package com.foodtraceability.customers.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodtraceability.customers.config.CustomersGlobalExceptionHandler;
import com.foodtraceability.customers.dto.*;
import com.foodtraceability.customers.entity.Complaint;
import com.foodtraceability.customers.entity.TraceabilityNode;
import com.foodtraceability.customers.service.ComplaintService;
import com.foodtraceability.customers.service.TraceabilityService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("接口请求/响应 JSON 演示")
class ControllerJsonDemoTest {

    @Mock private ComplaintService complaintService;
    @InjectMocks private ComplaintController complaintController;

    @Mock private TraceabilityService traceabilityService;
    @InjectMocks private TraceabilityController traceabilityController;

    private MockMvc complaintMvc;
    private MockMvc traceabilityMvc;
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private static final StringBuilder OUTPUT = new StringBuilder();

    @BeforeEach
    void setUp() {
        objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

        complaintMvc = MockMvcBuilders
                .standaloneSetup(complaintController)
                .setControllerAdvice(new CustomersGlobalExceptionHandler())
                .build();

        traceabilityMvc = MockMvcBuilders
                .standaloneSetup(traceabilityController)
                .setControllerAdvice(new CustomersGlobalExceptionHandler())
                .build();
    }

    @AfterAll
    static void writeOutput() throws Exception {
        File file = new File("target/api-json-demo.txt");
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write(OUTPUT.toString());
        }
        System.out.println("\n\nJSON demo output written to: " + file.getAbsolutePath());
    }

    private void appendHeader(String title) {
        OUTPUT.append("\n").append("=".repeat(72)).append("\n");
        OUTPUT.append("  ").append(title).append("\n");
        OUTPUT.append("=".repeat(72)).append("\n\n");
    }

    private void appendRequest(String method, String url, Object requestBody) throws Exception {
        OUTPUT.append(">>> REQUEST <<<\n");
        OUTPUT.append(method).append(" ").append(url).append("\n");
        OUTPUT.append("Content-Type: application/json\n");
        String body = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestBody);
        OUTPUT.append("\n").append(body).append("\n\n");
    }

    private void appendResponse(MvcResult result) throws Exception {
        OUTPUT.append(">>> RESPONSE <<<\n");
        OUTPUT.append("Status: ").append(result.getResponse().getStatus()).append("\n");
        OUTPUT.append("Content-Type: ").append(result.getResponse().getContentType()).append("\n\n");
        String body = result.getResponse().getContentAsString();
        // pretty print JSON response
        try {
            Object json = objectMapper.readValue(body, Object.class);
            body = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception ignored) {}
        OUTPUT.append(body).append("\n\n");
    }

    // ======================== 投诉接口 ========================

    @Test @Order(1)
    @DisplayName("1. POST /api/complaint/submit - 提交投诉(成功)")
    void demoSubmitSuccess() throws Exception {
        Complaint saved = new Complaint();
        saved.setId(1L); saved.setComplaintNo("TS2026062300001");
        saved.setProductBatchNo("BATCH-001"); saved.setProductName("有机奶粉");
        saved.setConsumerName("张三"); saved.setConsumerPhone("13800138000");
        saved.setComplaintType(1); saved.setComplaintTitle("产品包装破损");
        saved.setComplaintContent("收到产品时发现外包装严重破损，内部密封已破裂");
        saved.setImageUrls("https://img.example.com/1.jpg,https://img.example.com/2.jpg");
        saved.setStatus(0);
        saved.setCreateTime(LocalDateTime.of(2026, 6, 23, 10, 30, 0));
        saved.setUpdateTime(LocalDateTime.of(2026, 6, 23, 10, 30, 0));

        when(complaintService.submit(any(ComplaintSubmitDTO.class))).thenReturn(saved);

        ComplaintSubmitDTO dto = new ComplaintSubmitDTO();
        dto.setProductBatchNo("BATCH-001"); dto.setProductName("有机奶粉");
        dto.setConsumerName("张三"); dto.setConsumerPhone("13800138000");
        dto.setComplaintType(1); dto.setComplaintTitle("产品包装破损");
        dto.setComplaintContent("收到产品时发现外包装严重破损，内部密封已破裂");
        dto.setImageUrls("https://img.example.com/1.jpg,https://img.example.com/2.jpg");

        appendHeader("POST /api/complaint/submit - 提交投诉（成功）");
        appendRequest("POST", "/api/complaint/submit", dto);

        MvcResult result = complaintMvc.perform(post("/api/complaint/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        appendResponse(result);
    }

    @Test @Order(2)
    @DisplayName("2. POST /api/complaint/submit - 参数校验失败")
    void demoSubmitValidationError() throws Exception {
        ComplaintSubmitDTO dto = new ComplaintSubmitDTO();
        dto.setProductBatchNo("BATCH-001");
        dto.setConsumerPhone("12345");
        // 故意缺少 productName, consumerName, complaintType, complaintTitle, complaintContent
        // 同时手机号格式不正确

        appendHeader("POST /api/complaint/submit - 参数校验失败");
        appendRequest("POST", "/api/complaint/submit", dto);

        MvcResult result = complaintMvc.perform(post("/api/complaint/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andReturn();

        appendResponse(result);
        verify(complaintService, never()).submit(any());
    }

    @Test @Order(3)
    @DisplayName("3. POST /api/complaint/page - 分页查询")
    void demoPage() throws Exception {
        Complaint c1 = new Complaint();
        c1.setId(1L); c1.setComplaintNo("TS2026062300001"); c1.setProductName("有机奶粉");
        c1.setConsumerName("张三"); c1.setConsumerPhone("13800138000");
        c1.setComplaintType(1); c1.setComplaintTitle("产品包装破损");
        c1.setComplaintContent("外包装严重破损"); c1.setStatus(0);
        c1.setCreateTime(LocalDateTime.of(2026, 6, 23, 10, 30, 0));
        c1.setUpdateTime(LocalDateTime.of(2026, 6, 23, 10, 30, 0));

        Complaint c2 = new Complaint();
        c2.setId(2L); c2.setComplaintNo("TS2026062300002"); c2.setProductName("鲜牛奶");
        c2.setConsumerName("李四"); c2.setConsumerPhone("13900139000");
        c2.setComplaintType(2); c2.setComplaintTitle("产品有异味");
        c2.setComplaintContent("打开后发现有异味"); c2.setStatus(0);
        c2.setCreateTime(LocalDateTime.of(2026, 6, 23, 11, 0, 0));
        c2.setUpdateTime(LocalDateTime.of(2026, 6, 23, 11, 0, 0));

        Page<Complaint> mockPage = new Page<>(1, 10);
        mockPage.setTotal(2); mockPage.setPages(1);
        mockPage.setRecords(Arrays.asList(c1, c2));

        when(complaintService.page(any(ComplaintQueryDTO.class))).thenReturn(mockPage);

        ComplaintQueryDTO queryDTO = new ComplaintQueryDTO();
        queryDTO.setStatus(0); queryDTO.setPageNum(1); queryDTO.setPageSize(10);

        appendHeader("POST /api/complaint/page - 分页查询（status=0 待处理）");
        appendRequest("POST", "/api/complaint/page", queryDTO);

        MvcResult result = complaintMvc.perform(post("/api/complaint/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        appendResponse(result);
    }

    @Test @Order(4)
    @DisplayName("4. GET /api/complaint/detail/{id} - 投诉详情")
    void demoDetail() throws Exception {
        Complaint detail = new Complaint();
        detail.setId(1L); detail.setComplaintNo("TS2026062300001");
        detail.setProductBatchNo("BATCH-001"); detail.setProductName("有机奶粉");
        detail.setConsumerName("张三"); detail.setConsumerPhone("13800138000");
        detail.setComplaintType(1); detail.setComplaintTitle("产品包装破损");
        detail.setComplaintContent("收到产品时发现外包装严重破损，内部密封已破裂");
        detail.setImageUrls("https://img.example.com/1.jpg,https://img.example.com/2.jpg");
        detail.setStatus(0);
        detail.setCreateTime(LocalDateTime.of(2026, 6, 23, 10, 30, 0));
        detail.setUpdateTime(LocalDateTime.of(2026, 6, 23, 10, 30, 0));

        when(complaintService.detail(1L)).thenReturn(detail);

        appendHeader("GET /api/complaint/detail/1 - 投诉详情");
        appendRequest("GET", "/api/complaint/detail/1", "(无请求体)");

        MvcResult result = complaintMvc.perform(get("/api/complaint/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        appendResponse(result);
    }

    @Test @Order(5)
    @DisplayName("5. PUT /api/complaint/feedback - 处理反馈")
    void demoFeedback() throws Exception {
        Complaint feedbackResult = new Complaint();
        feedbackResult.setId(1L); feedbackResult.setComplaintNo("TS2026062300001");
        feedbackResult.setStatus(2);
        feedbackResult.setFeedbackContent("已联系消费者致歉并安排补发，消费者表示满意");
        feedbackResult.setFeedbackBy("客服管理员");
        feedbackResult.setFeedbackTime(LocalDateTime.of(2026, 6, 23, 14, 0, 0));

        when(complaintService.feedback(any(ComplaintFeedbackDTO.class))).thenReturn(feedbackResult);

        ComplaintFeedbackDTO dto = new ComplaintFeedbackDTO();
        dto.setComplaintId(1L);
        dto.setFeedbackContent("已联系消费者致歉并安排补发，消费者表示满意");
        dto.setFeedbackBy("客服管理员");

        appendHeader("PUT /api/complaint/feedback - 处理反馈");
        appendRequest("PUT", "/api/complaint/feedback", dto);

        MvcResult result = complaintMvc.perform(put("/api/complaint/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        appendResponse(result);
    }

    // ======================== 溯源接口 ========================

    @Test @Order(6)
    @DisplayName("6. POST /api/traceability/query - 扫码查询溯源")
    void demoQueryTraceability() throws Exception {
        TraceabilityNode node1 = new TraceabilityNode();
        node1.setId(1L); node1.setProductBatchNo("BATCH-20260623-001");
        node1.setNodeName("原材料采购");
        node1.setNodeDescription("从内蒙古有机牧场采购新鲜有机原料奶");
        node1.setNodeTime(LocalDateTime.of(2026, 6, 1, 8, 0, 0));
        node1.setLocation("内蒙古呼和浩特"); node1.setOperator("采购部");
        node1.setSortOrder(1);

        TraceabilityNode node2 = new TraceabilityNode();
        node2.setId(2L); node2.setProductBatchNo("BATCH-20260623-001");
        node2.setNodeName("生产加工");
        node2.setNodeDescription("经过巴氏杀菌、均质、灌装等工艺流程");
        node2.setNodeTime(LocalDateTime.of(2026, 6, 5, 10, 30, 0));
        node2.setLocation("河北石家庄"); node2.setOperator("生产车间");
        node2.setSortOrder(2);

        TraceabilityNode node3 = new TraceabilityNode();
        node3.setId(3L); node3.setProductBatchNo("BATCH-20260623-001");
        node3.setNodeName("质量检测");
        node3.setNodeDescription("通过微生物、重金属、营养成分等多项检测合格");
        node3.setNodeTime(LocalDateTime.of(2026, 6, 7, 14, 0, 0));
        node3.setLocation("河北石家庄"); node3.setOperator("质检部");
        node3.setSortOrder(3);

        TraceabilityVO vo = new TraceabilityVO();
        vo.setProductBatchNo("BATCH-20260623-001"); vo.setProductName("有机奶粉");
        vo.setProductSpec("800g/罐"); vo.setManufacturer("XX乳业有限公司");
        vo.setOrigin("内蒙古");
        vo.setProductionDate(LocalDate.of(2026, 6, 5));
        vo.setExpirationDate(LocalDate.of(2028, 6, 4));
        vo.setNodes(Arrays.asList(node1, node2, node3));

        when(traceabilityService.queryByBatchNo("BATCH-20260623-001")).thenReturn(vo);
        doNothing().when(traceabilityService).recordScan(anyString(), anyString(), any(), any());

        TraceabilityQueryDTO dto = new TraceabilityQueryDTO();
        dto.setProductBatchNo("BATCH-20260623-001");

        appendHeader("POST /api/traceability/query - 扫码查询溯源");
        appendRequest("POST", "/api/traceability/query", dto);

        MvcResult result = traceabilityMvc.perform(post("/api/traceability/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        appendResponse(result);
    }

    @Test @Order(7)
    @DisplayName("7. POST /api/traceability/scan - 记录扫码事件")
    void demoScan() throws Exception {
        doNothing().when(traceabilityService).recordScan(anyString(), anyString(), anyString(), anyString());

        ScanRecordDTO dto = new ScanRecordDTO();
        dto.setProductBatchNo("BATCH-20260623-001");
        dto.setScanIp("192.168.1.100");
        dto.setScanLocation("北京市朝阳区");

        appendHeader("POST /api/traceability/scan - 记录扫码事件");
        appendRequest("POST", "/api/traceability/scan", dto);

        MvcResult result = traceabilityMvc.perform(post("/api/traceability/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        appendResponse(result);
    }
}

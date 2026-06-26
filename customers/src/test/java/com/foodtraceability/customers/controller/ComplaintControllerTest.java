package com.foodtraceability.customers.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodtraceability.customers.config.CustomersGlobalExceptionHandler;
import com.foodtraceability.customers.dto.ComplaintQueryDTO;
import com.foodtraceability.customers.dto.ComplaintSubmitDTO;
import com.foodtraceability.customers.entity.Complaint;
import com.foodtraceability.customers.service.ComplaintService;
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

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("消费者投诉控制器 - 接口测试")
class ComplaintControllerTest {

    @Mock
    private ComplaintService complaintService;

    @InjectMocks
    private ComplaintController complaintController;

    private MockMvc mockMvc;

    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private Complaint mockComplaint;

    @BeforeEach
    void setUp() {
        objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(complaintController)
                .setControllerAdvice(new CustomersGlobalExceptionHandler())
                .build();

        mockComplaint = new Complaint();
        mockComplaint.setComplaintRecordId(1L);
        mockComplaint.setComplaintNo("TS2026062300001");
        mockComplaint.setBatchNumber("BATCH-001");
        mockComplaint.setEnterpriseName("有机奶粉公司");
        mockComplaint.setComplainantName("张三");
        mockComplaint.setPhone("13800138000");
        mockComplaint.setComplaintType(1);
        mockComplaint.setDescription("收到产品时发现外包装严重破损");
        mockComplaint.setPhotoUrls("https://img.example.com/1.jpg");
        mockComplaint.setStatus(1);
        mockComplaint.setSubmitTime(LocalDateTime.now());
        mockComplaint.setCreateTime(LocalDateTime.now());
        mockComplaint.setUpdateTime(LocalDateTime.now());
    }

    @Nested
    @DisplayName("POST /api/complaint/submit - 提交投诉")
    class SubmitTests {

        @Test
        @DisplayName("正常提交投诉，返回200和投诉记录")
        void testSubmitSuccess() throws Exception {
            when(complaintService.submit(any(ComplaintSubmitDTO.class)))
                    .thenReturn(mockComplaint);

            ComplaintSubmitDTO dto = new ComplaintSubmitDTO();
            dto.setBatchNumber("BATCH-001");
            dto.setEnterpriseName("有机奶粉公司");
            dto.setComplainantName("张三");
            dto.setPhone("13800138000");
            dto.setComplaintType(1);
            dto.setDescription("收到产品时发现外包装严重破损");

            mockMvc.perform(post("/api/complaint/submit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.complaintNo").value("TS2026062300001"))
                    .andExpect(jsonPath("$.data.enterpriseName").value("有机奶粉公司"))
                    .andExpect(jsonPath("$.data.complainantName").value("张三"))
                    .andExpect(jsonPath("$.data.status").value(1));
        }

        @Test
        @DisplayName("缺少必填字段时返回400错误")
        void testSubmitMissingRequiredFields() throws Exception {
            ComplaintSubmitDTO dto = new ComplaintSubmitDTO();

            mockMvc.perform(post("/api/complaint/submit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message", containsString("不能为空")));

            verify(complaintService, never()).submit(any());
        }

        @Test
        @DisplayName("手机号格式错误时返回400")
        void testSubmitInvalidPhone() throws Exception {
            ComplaintSubmitDTO dto = new ComplaintSubmitDTO();
            dto.setBatchNumber("BATCH-001");
            dto.setEnterpriseName("有机奶粉公司");
            dto.setComplainantName("张三");
            dto.setPhone("12345");
            dto.setComplaintType(1);
            dto.setDescription("收到产品时发现外包装严重破损");

            mockMvc.perform(post("/api/complaint/submit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message", containsString("手机号格式不正确")));

            verify(complaintService, never()).submit(any());
        }

        @Test
        @DisplayName("投诉类型超出范围时返回400")
        void testSubmitInvalidComplaintType() throws Exception {
            ComplaintSubmitDTO dto = new ComplaintSubmitDTO();
            dto.setBatchNumber("BATCH-001");
            dto.setEnterpriseName("有机奶粉公司");
            dto.setComplainantName("张三");
            dto.setPhone("13800138000");
            dto.setComplaintType(99);
            dto.setDescription("收到产品时发现外包装严重破损");

            mockMvc.perform(post("/api/complaint/submit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message", containsString("投诉类型必须在1-5之间")));

            verify(complaintService, never()).submit(any());
        }
    }

    @Nested
    @DisplayName("POST /api/complaint/page - 分页查询")
    class PageTests {

        @Test
        @DisplayName("正常分页查询，返回分页结果")
        void testPageSuccess() throws Exception {
            Page<Complaint> mockPage = new Page<>(1, 10);
            mockPage.setTotal(2);
            mockPage.setPages(1);
            mockPage.setRecords(Arrays.asList(mockComplaint));

            when(complaintService.page(any(ComplaintQueryDTO.class)))
                    .thenReturn(mockPage);

            mockMvc.perform(post("/api/complaint/page")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(2))
                    .andExpect(jsonPath("$.data.records.length()").value(1))
                    .andExpect(jsonPath("$.data.records[0].complainantName").value("张三"));
        }

        @Test
        @DisplayName("空条件查询，使用默认分页参数")
        void testPageWithEmptyBody() throws Exception {
            Page<Complaint> mockPage = new Page<>(1, 10);
            mockPage.setTotal(0);
            mockPage.setRecords(java.util.Collections.emptyList());

            when(complaintService.page(any(ComplaintQueryDTO.class)))
                    .thenReturn(mockPage);

            mockMvc.perform(post("/api/complaint/page")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(0));
        }

        @Test
        @DisplayName("带筛选条件的分页查询")
        void testPageWithFilters() throws Exception {
            Page<Complaint> mockPage = new Page<>(1, 10);
            mockPage.setTotal(1);
            mockPage.setRecords(java.util.Collections.singletonList(mockComplaint));

            when(complaintService.page(any(ComplaintQueryDTO.class)))
                    .thenReturn(mockPage);

            ComplaintQueryDTO queryDTO = new ComplaintQueryDTO();
            queryDTO.setStatus(1);
            queryDTO.setComplaintType(1);
            queryDTO.setPageNum(1);
            queryDTO.setPageSize(10);

            mockMvc.perform(post("/api/complaint/page")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(queryDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/complaint/detail/{id} - 投诉详情")
    class DetailTests {

        @Test
        @DisplayName("查询存在的投诉记录，返回200")
        void testDetailSuccess() throws Exception {
            when(complaintService.detail(1L)).thenReturn(mockComplaint);

            mockMvc.perform(get("/api/complaint/detail/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.complaintRecordId").value(1))
                    .andExpect(jsonPath("$.data.complaintNo").value("TS2026062300001"))
                    .andExpect(jsonPath("$.data.complainantName").value("张三"))
                    .andExpect(jsonPath("$.data.enterpriseName").value("有机奶粉公司"));
        }

        @Test
        @DisplayName("查询不存在的投诉记录，返回500")
        void testDetailNotFound() throws Exception {
            when(complaintService.detail(999L))
                    .thenThrow(new RuntimeException("投诉记录不存在"));

            mockMvc.perform(get("/api/complaint/detail/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("投诉记录不存在"));
        }
    }
}

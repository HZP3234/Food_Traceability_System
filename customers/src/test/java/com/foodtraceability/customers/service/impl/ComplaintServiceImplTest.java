package com.foodtraceability.customers.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodtraceability.customers.dto.ComplaintQueryDTO;
import com.foodtraceability.customers.dto.ComplaintSubmitDTO;
import com.foodtraceability.customers.entity.Complaint;
import com.foodtraceability.customers.mapper.ComplaintMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("消费者投诉服务 - 单元测试")
class ComplaintServiceImplTest {

    @Mock
    private ComplaintMapper complaintMapper;

    @InjectMocks
    private ComplaintServiceImpl complaintService;

    private ComplaintSubmitDTO submitDTO;
    private Complaint savedComplaint;

    @BeforeEach
    void setUp() {
        submitDTO = new ComplaintSubmitDTO();
        submitDTO.setBatchNumber("BATCH-001");
        submitDTO.setEnterpriseName("有机奶粉公司");
        submitDTO.setComplainantName("张三");
        submitDTO.setPhone("13800138000");
        submitDTO.setComplaintType(1);
        submitDTO.setDescription("收到产品时发现外包装严重破损");
        submitDTO.setPhotoUrls("https://img.example.com/1.jpg");

        savedComplaint = new Complaint();
        savedComplaint.setComplaintRecordId(1L);
        savedComplaint.setComplaintNo("TS2026062300001");
        savedComplaint.setBatchNumber("BATCH-001");
        savedComplaint.setEnterpriseName("有机奶粉公司");
        savedComplaint.setComplainantName("张三");
        savedComplaint.setPhone("13800138000");
        savedComplaint.setComplaintType(1);
        savedComplaint.setDescription("收到产品时发现外包装严重破损");
        savedComplaint.setPhotoUrls("https://img.example.com/1.jpg");
        savedComplaint.setStatus(1);
        savedComplaint.setSubmitTime(LocalDateTime.now());
        savedComplaint.setCreateTime(LocalDateTime.now());
        savedComplaint.setUpdateTime(LocalDateTime.now());
    }

    @Nested
    @DisplayName("提交投诉 - submit")
    class SubmitTests {

        @Test
        @DisplayName("正常提交投诉，返回带投诉编号的完整记录")
        void testSubmitSuccess() {
            when(complaintMapper.insert((Complaint) any())).thenReturn(1);

            Complaint result = complaintService.submit(submitDTO);

            assertNotNull(result);
            assertNotNull(result.getComplaintNo());
            assertTrue(result.getComplaintNo().startsWith("TS"));
            assertEquals(15, result.getComplaintNo().length());

            assertEquals("BATCH-001", result.getBatchNumber());
            assertEquals("有机奶粉公司", result.getEnterpriseName());
            assertEquals("张三", result.getComplainantName());
            assertEquals("13800138000", result.getPhone());
            assertEquals(1, result.getComplaintType());
            assertEquals("收到产品时发现外包装严重破损", result.getDescription());
            assertEquals("https://img.example.com/1.jpg", result.getPhotoUrls());
            assertEquals(1, result.getStatus());

            verify(complaintMapper, times(1)).insert(any(Complaint.class));
        }

        @Test
        @DisplayName("提交投诉时，状态默认为已提交(1)")
        void testSubmitDefaultStatus() {
            when(complaintMapper.insert((Complaint) any())).thenReturn(1);

            Complaint result = complaintService.submit(submitDTO);
            assertEquals(1, result.getStatus());
        }

        @Test
        @DisplayName("投诉编号格式正确：TS + 日期 + 5位随机数")
        void testComplaintNoFormat() {
            when(complaintMapper.insert((Complaint) any())).thenReturn(1);

            Complaint result = complaintService.submit(submitDTO);

            String no = result.getComplaintNo();
            assertTrue(no.startsWith("TS"), "投诉编号应以TS开头");
            assertTrue(no.matches("^TS\\d{13}$"), "投诉编号格式应为TS+yyyyMMdd+5位数字，实际: " + no);
        }

        @Test
        @DisplayName("提交的投诉信息被完整传递给Mapper")
        void testSubmitPassesAllFieldsToMapper() {
            ArgumentCaptor<Complaint> captor = ArgumentCaptor.forClass(Complaint.class);
            when(complaintMapper.insert(captor.capture())).thenReturn(1);

            complaintService.submit(submitDTO);

            Complaint captured = captor.getValue();
            assertEquals(submitDTO.getBatchNumber(), captured.getBatchNumber());
            assertEquals(submitDTO.getEnterpriseName(), captured.getEnterpriseName());
            assertEquals(submitDTO.getComplainantName(), captured.getComplainantName());
            assertEquals(submitDTO.getPhone(), captured.getPhone());
            assertEquals(submitDTO.getComplaintType(), captured.getComplaintType());
            assertEquals(submitDTO.getDescription(), captured.getDescription());
            assertEquals(submitDTO.getPhotoUrls(), captured.getPhotoUrls());
        }
    }

    @Nested
    @DisplayName("分页查询 - page")
    class PageTests {

        @Test
        @DisplayName("无条件分页查询，使用默认分页参数")
        void testPageWithDefaults() {
            ComplaintQueryDTO queryDTO = new ComplaintQueryDTO();
            Page<Complaint> mockPage = new Page<>(1, 10);
            mockPage.setTotal(0);
            mockPage.setRecords(java.util.Collections.emptyList());

            when(complaintMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            Page<Complaint> result = complaintService.page(queryDTO);

            assertNotNull(result);
            assertEquals(0, result.getTotal());
            verify(complaintMapper, times(1))
                    .selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("按投诉编号精确查询")
        void testPageByComplaintNo() {
            ComplaintQueryDTO queryDTO = new ComplaintQueryDTO();
            queryDTO.setComplaintNo("TS2026062300001");

            Page<Complaint> mockPage = new Page<>(1, 10);
            mockPage.setTotal(1);
            mockPage.setRecords(java.util.Collections.singletonList(savedComplaint));

            when(complaintMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            Page<Complaint> result = complaintService.page(queryDTO);

            assertEquals(1, result.getTotal());
            assertEquals(1, result.getRecords().size());
        }

        @Test
        @DisplayName("多条件组合查询：批次+投诉类型+状态")
        void testPageWithMultipleFilters() {
            ComplaintQueryDTO queryDTO = new ComplaintQueryDTO();
            queryDTO.setBatchNumber("BATCH-001");
            queryDTO.setComplaintType(1);
            queryDTO.setStatus(1);
            queryDTO.setPageNum(2);
            queryDTO.setPageSize(5);

            Page<Complaint> mockPage = new Page<>(2, 5);
            mockPage.setTotal(1);
            mockPage.setRecords(java.util.Collections.singletonList(savedComplaint));

            when(complaintMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            Page<Complaint> result = complaintService.page(queryDTO);

            assertNotNull(result);
            verify(complaintMapper, times(1))
                    .selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("分页参数正确传递到Mapper")
        void testPageParametersPassedCorrectly() {
            ComplaintQueryDTO queryDTO = new ComplaintQueryDTO();
            queryDTO.setPageNum(3);
            queryDTO.setPageSize(20);

            ArgumentCaptor<Page<Complaint>> pageCaptor = ArgumentCaptor.forClass(Page.class);
            when(complaintMapper.selectPage(pageCaptor.capture(), any(LambdaQueryWrapper.class)))
                    .thenReturn(new Page<>(3, 20));

            complaintService.page(queryDTO);

            Page<Complaint> capturedPage = pageCaptor.getValue();
            assertEquals(3, capturedPage.getCurrent());
            assertEquals(20, capturedPage.getSize());
        }
    }

    @Nested
    @DisplayName("投诉详情 - detail")
    class DetailTests {

        @Test
        @DisplayName("根据ID查询存在的投诉记录")
        void testDetailSuccess() {
            when(complaintMapper.selectById(1L)).thenReturn(savedComplaint);

            Complaint result = complaintService.detail(1L);

            assertNotNull(result);
            assertEquals(1L, result.getComplaintRecordId());
            assertEquals("TS2026062300001", result.getComplaintNo());
            assertEquals("张三", result.getComplainantName());
        }

        @Test
        @DisplayName("查询不存在的投诉记录，抛出RuntimeException")
        void testDetailNotFound() {
            when(complaintMapper.selectById(999L)).thenReturn(null);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> complaintService.detail(999L));

            assertEquals("投诉记录不存在", exception.getMessage());
        }
    }

    @Test
    @DisplayName("完整业务流程：提交 -> 查询")
    void testFullBusinessFlow() {
        // 1. 提交投诉
        when(complaintMapper.insert((Complaint) any())).thenReturn(1);

        Complaint submitted = complaintService.submit(submitDTO);
        assertNotNull(submitted);
        assertEquals(1, submitted.getStatus());

        // 2. 查询投诉详情
        when(complaintMapper.selectById(1L)).thenReturn(savedComplaint);

        Complaint detail = complaintService.detail(1L);
        assertNotNull(detail);
        assertEquals("TS2026062300001", detail.getComplaintNo());
    }
}

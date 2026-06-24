package com.foodtraceability.customers.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodtraceability.customers.entity.Complaint;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ComplaintMapperTest {

    private static SqlSessionFactory sqlSessionFactory;
    private static SqlSession sqlSession;
    private static ComplaintMapper complaintMapper;
    private static Long savedId;

    @BeforeAll
    static void setUp() {
        DataSource dataSource = new PooledDataSource(
                "org.h2.Driver",
                "jdbc:h2:mem:food_traceability;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                "sa",
                ""
        );

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
        configuration.addInterceptor(interceptor);

        Environment environment = new Environment("test",
                new JdbcTransactionFactory(), dataSource);
        configuration.setEnvironment(environment);

        configuration.addMapper(ComplaintMapper.class);

        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        globalConfig.getDbConfig().setIdType(com.baomidou.mybatisplus.annotation.IdType.AUTO);
        GlobalConfigUtils.setGlobalConfig(configuration, globalConfig);

        sqlSessionFactory = new MybatisSqlSessionFactoryBuilder().build(configuration);

        // 建表
        try (SqlSession session = sqlSessionFactory.openSession()) {
            session.getConnection().createStatement().execute(
                    "CREATE TABLE t_complaint_record (" +
                    "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "  complaint_no VARCHAR(32) NOT NULL," +
                    "  product_batch_no VARCHAR(64)," +
                    "  product_name VARCHAR(128)," +
                    "  consumer_name VARCHAR(64)," +
                    "  consumer_phone VARCHAR(20)," +
                    "  complaint_type TINYINT NOT NULL," +
                    "  complaint_title VARCHAR(256) NOT NULL," +
                    "  complaint_content TEXT NOT NULL," +
                    "  image_urls VARCHAR(1024)," +
                    "  status TINYINT NOT NULL DEFAULT 0," +
                    "  feedback_content TEXT," +
                    "  feedback_time DATETIME," +
                    "  feedback_by VARCHAR(64)," +
                    "  create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "  update_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "  deleted TINYINT NOT NULL DEFAULT 0" +
                    ")"
            );
            session.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create table", e);
        }

        sqlSession = sqlSessionFactory.openSession(true);
        complaintMapper = sqlSession.getMapper(ComplaintMapper.class);
    }

    @AfterAll
    static void tearDown() {
        if (sqlSession != null) {
            sqlSession.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("插入投诉记录")
    void testInsert() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Complaint complaint = new Complaint();
        complaint.setComplaintNo("TS2026062300001");
        complaint.setProductBatchNo("BATCH-20260623-001");
        complaint.setProductName("有机奶粉");
        complaint.setConsumerName("张三");
        complaint.setConsumerPhone("13800138000");
        complaint.setComplaintType(1);
        complaint.setComplaintTitle("产品包装破损");
        complaint.setComplaintContent("收到产品时发现外包装严重破损，内部密封已打开");
        complaint.setImageUrls("https://img.example.com/1.jpg,https://img.example.com/2.jpg");
        complaint.setStatus(0);
        complaint.setCreateTime(now);
        complaint.setUpdateTime(now);

        int rows = complaintMapper.insert(complaint);
        assertEquals(1, rows);
        assertNotNull(complaint.getId());
        savedId = complaint.getId();
    }

    @Test
    @Order(2)
    @DisplayName("根据ID查询投诉记录")
    void testSelectById() {
        Complaint complaint = complaintMapper.selectById(savedId);
        assertNotNull(complaint);
        assertEquals("TS2026062300001", complaint.getComplaintNo());
        assertEquals("有机奶粉", complaint.getProductName());
        assertEquals("张三", complaint.getConsumerName());
        assertEquals("13800138000", complaint.getConsumerPhone());
        assertEquals(1, complaint.getComplaintType());
        assertEquals(0, complaint.getStatus());
        assertEquals("https://img.example.com/1.jpg,https://img.example.com/2.jpg", complaint.getImageUrls());
    }

    @Test
    @Order(3)
    @DisplayName("分页查询投诉记录")
    void testSelectPage() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Complaint c2 = new Complaint();
        c2.setComplaintNo("TS2026062300002");
        c2.setProductBatchNo("BATCH-20260623-002");
        c2.setProductName("鲜牛奶");
        c2.setConsumerName("李四");
        c2.setConsumerPhone("13900139000");
        c2.setComplaintType(2);
        c2.setComplaintTitle("产品有异味");
        c2.setComplaintContent("打开后发现牛奶有酸臭味，疑似变质");
        c2.setStatus(0);
        c2.setCreateTime(now);
        c2.setUpdateTime(now);
        complaintMapper.insert(c2);

        Page<Complaint> page1 = new Page<>(1, 1);
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Complaint::getId);
        Page<Complaint> result1 = complaintMapper.selectPage(page1, wrapper);
        assertEquals(2, result1.getTotal());
        assertEquals(2, result1.getPages());
        assertEquals(1, result1.getRecords().size());
        assertEquals("TS2026062300001", result1.getRecords().get(0).getComplaintNo());

        Page<Complaint> page2 = new Page<>(2, 1);
        Page<Complaint> result2 = complaintMapper.selectPage(page2, wrapper);
        assertEquals(1, result2.getRecords().size());
        assertEquals("TS2026062300002", result2.getRecords().get(0).getComplaintNo());
    }

    @Test
    @Order(4)
    @DisplayName("按条件查询投诉记录")
    void testSelectByCondition() {
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Complaint::getProductBatchNo, "BATCH-20260623-001");
        List<Complaint> list = complaintMapper.selectList(wrapper);
        assertEquals(1, list.size());
        assertEquals("张三", list.get(0).getConsumerName());
    }

    @Test
    @Order(5)
    @DisplayName("更新投诉记录")
    void testUpdateById() {
        Complaint complaint = complaintMapper.selectById(savedId);
        complaint.setStatus(1);
        complaint.setFeedbackContent("已收到您的投诉，正在处理中");
        int rows = complaintMapper.updateById(complaint);
        assertEquals(1, rows);

        Complaint updated = complaintMapper.selectById(savedId);
        assertEquals(1, updated.getStatus());
        assertEquals("已收到您的投诉，正在处理中", updated.getFeedbackContent());
    }

    @Test
    @Order(6)
    @DisplayName("物理删除投诉记录")
    void testDeleteById() {
        int rows = complaintMapper.deleteById(savedId);
        assertEquals(1, rows);

        Complaint deleted = complaintMapper.selectById(savedId);
        assertNull(deleted);
    }
}

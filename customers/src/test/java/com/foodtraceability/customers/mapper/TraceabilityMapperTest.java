package com.foodtraceability.customers.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.foodtraceability.customers.entity.ProductTraceability;
import com.foodtraceability.customers.entity.ScanRecord;
import com.foodtraceability.customers.entity.TraceabilityNode;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TraceabilityMapperTest {

    private static SqlSessionFactory sqlSessionFactory;
    private static SqlSession sqlSession;
    private static ProductTraceabilityMapper productTraceabilityMapper;
    private static TraceabilityNodeMapper traceabilityNodeMapper;
    private static ScanRecordMapper scanRecordMapper;
    private static Long savedProductId;

    @BeforeAll
    static void setUp() {
        DataSource dataSource = new PooledDataSource(
                "org.h2.Driver",
                "jdbc:h2:mem:food_traceability2;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
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

        configuration.addMapper(ProductTraceabilityMapper.class);
        configuration.addMapper(TraceabilityNodeMapper.class);
        configuration.addMapper(ScanRecordMapper.class);

        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        globalConfig.getDbConfig().setIdType(com.baomidou.mybatisplus.annotation.IdType.AUTO);
        GlobalConfigUtils.setGlobalConfig(configuration, globalConfig);

        sqlSessionFactory = new MybatisSqlSessionFactoryBuilder().build(configuration);

        try (SqlSession session = sqlSessionFactory.openSession()) {
            session.getConnection().createStatement().execute(
                    "CREATE TABLE product_traceability (" +
                    "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "  product_batch_no VARCHAR(64) NOT NULL," +
                    "  product_name VARCHAR(128) NOT NULL," +
                    "  product_spec VARCHAR(128)," +
                    "  manufacturer VARCHAR(256)," +
                    "  origin VARCHAR(256)," +
                    "  production_date DATE," +
                    "  expiration_date DATE," +
                    "  qr_code_url VARCHAR(512)," +
                    "  create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "  update_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "  deleted TINYINT NOT NULL DEFAULT 0" +
                    ")"
            );
            session.getConnection().createStatement().execute(
                    "CREATE TABLE traceability_node (" +
                    "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "  product_batch_no VARCHAR(64) NOT NULL," +
                    "  node_name VARCHAR(128) NOT NULL," +
                    "  node_description TEXT," +
                    "  node_time DATETIME," +
                    "  location VARCHAR(256)," +
                    "  operator VARCHAR(128)," +
                    "  sort_order INT NOT NULL DEFAULT 0," +
                    "  create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "  update_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "  deleted TINYINT NOT NULL DEFAULT 0" +
                    ")"
            );
            session.getConnection().createStatement().execute(
                    "CREATE TABLE scan_record (" +
                    "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "  product_batch_no VARCHAR(64) NOT NULL," +
                    "  scan_ip VARCHAR(64)," +
                    "  scan_location VARCHAR(256)," +
                    "  scan_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "  create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "  update_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "  deleted TINYINT NOT NULL DEFAULT 0" +
                    ")"
            );
            session.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create tables", e);
        }

        sqlSession = sqlSessionFactory.openSession(true);
        productTraceabilityMapper = sqlSession.getMapper(ProductTraceabilityMapper.class);
        traceabilityNodeMapper = sqlSession.getMapper(TraceabilityNodeMapper.class);
        scanRecordMapper = sqlSession.getMapper(ScanRecordMapper.class);
    }

    @AfterAll
    static void tearDown() {
        if (sqlSession != null) {
            sqlSession.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("插入产品溯源信息")
    void testInsertProduct() {
        ProductTraceability product = new ProductTraceability();
        product.setProductBatchNo("BATCH-20260623-001");
        product.setProductName("有机奶粉");
        product.setProductSpec("800g/罐");
        product.setManufacturer("XX乳业有限公司");
        product.setOrigin("内蒙古");
        product.setProductionDate(LocalDate.of(2026, 6, 5));
        product.setExpirationDate(LocalDate.of(2028, 6, 4));

        int rows = productTraceabilityMapper.insert(product);
        assertEquals(1, rows);
        assertNotNull(product.getId());
        savedProductId = product.getId();
    }

    @Test
    @Order(2)
    @DisplayName("根据批次号查询产品溯源信息")
    void testSelectProductByBatchNo() {
        LambdaQueryWrapper<ProductTraceability> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductTraceability::getProductBatchNo, "BATCH-20260623-001");
        ProductTraceability product = productTraceabilityMapper.selectOne(wrapper);

        assertNotNull(product);
        assertEquals("有机奶粉", product.getProductName());
        assertEquals("800g/罐", product.getProductSpec());
        assertEquals("XX乳业有限公司", product.getManufacturer());
        assertEquals("内蒙古", product.getOrigin());
        assertEquals(LocalDate.of(2026, 6, 5), product.getProductionDate());
        assertEquals(LocalDate.of(2028, 6, 4), product.getExpirationDate());
    }

    @Test
    @Order(3)
    @DisplayName("插入溯源节点")
    void testInsertTraceabilityNodes() {
        TraceabilityNode node1 = new TraceabilityNode();
        node1.setProductBatchNo("BATCH-20260623-001");
        node1.setNodeName("原材料采购");
        node1.setNodeDescription("采购有机原料");
        node1.setNodeTime(LocalDateTime.of(2026, 6, 1, 8, 0));
        node1.setLocation("内蒙古呼和浩特");
        node1.setOperator("采购部");
        node1.setSortOrder(1);
        int rows1 = traceabilityNodeMapper.insert(node1);
        assertEquals(1, rows1);
        assertNotNull(node1.getId());

        TraceabilityNode node2 = new TraceabilityNode();
        node2.setProductBatchNo("BATCH-20260623-001");
        node2.setNodeName("生产加工");
        node2.setNodeDescription("经过高温灭菌处理");
        node2.setNodeTime(LocalDateTime.of(2026, 6, 5, 10, 30));
        node2.setLocation("河北石家庄");
        node2.setOperator("生产车间");
        node2.setSortOrder(2);
        int rows2 = traceabilityNodeMapper.insert(node2);
        assertEquals(1, rows2);
        assertNotNull(node2.getId());
    }

    @Test
    @Order(4)
    @DisplayName("根据批次号查询溯源节点，按sort_order升序")
    void testSelectNodesByBatchNo() {
        LambdaQueryWrapper<TraceabilityNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TraceabilityNode::getProductBatchNo, "BATCH-20260623-001");
        wrapper.orderByAsc(TraceabilityNode::getSortOrder);
        List<TraceabilityNode> nodes = traceabilityNodeMapper.selectList(wrapper);

        assertEquals(2, nodes.size());
        assertEquals("原材料采购", nodes.get(0).getNodeName());
        assertEquals(1, nodes.get(0).getSortOrder());
        assertEquals("生产加工", nodes.get(1).getNodeName());
        assertEquals(2, nodes.get(1).getSortOrder());
    }

    @Test
    @Order(5)
    @DisplayName("插入扫码记录")
    void testInsertScanRecord() {
        ScanRecord record = new ScanRecord();
        record.setProductBatchNo("BATCH-20260623-001");
        record.setScanIp("192.168.1.100");
        record.setScanLocation("北京市朝阳区");
        record.setScanTime(LocalDateTime.now());

        int rows = scanRecordMapper.insert(record);
        assertEquals(1, rows);
        assertNotNull(record.getId());
    }

    @Test
    @Order(6)
    @DisplayName("查询扫码记录")
    void testSelectScanRecords() {
        LambdaQueryWrapper<ScanRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScanRecord::getProductBatchNo, "BATCH-20260623-001");
        List<ScanRecord> records = scanRecordMapper.selectList(wrapper);

        assertEquals(1, records.size());
        assertEquals("BATCH-20260623-001", records.get(0).getProductBatchNo());
        assertEquals("192.168.1.100", records.get(0).getScanIp());
        assertEquals("北京市朝阳区", records.get(0).getScanLocation());
        assertNotNull(records.get(0).getScanTime());
    }
}

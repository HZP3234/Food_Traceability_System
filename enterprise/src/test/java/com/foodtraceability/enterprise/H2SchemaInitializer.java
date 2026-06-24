package com.foodtraceability.enterprise;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

/** Creates the in-memory test schema from entity metadata, keeping H2 aligned with module fields. */
@Component
@ConditionalOnProperty(name = "spring.datasource.driver-class-name", havingValue = "org.h2.Driver")
class H2SchemaInitializer {
    private final JdbcTemplate jdbc;

    H2SchemaInitializer(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @PostConstruct
    void initialize() {
        List<Class<?>> entities = List.of(
            entity("CcReceipt"), entity("CcTempHumidity"), entity("CcTransport"), entity("CcTransportNode"),
            entity("CcVehicle"), entity("ProcessBatch"), entity("ProdBatch"), entity("ProdEnvRecord"),
            entity("ProdMaterialInput"), entity("QualityInspection"), entity("Raw"), entity("RawDetail"),
            entity("RawPending"), entity("SalesStock"), entity("SalesStorage"), entity("SalesSupplement"),
            entity("SalesTerminal"), entity("TechTemplate"), entity("TraceCode"), entity("TraceCodeBind"), entity("Warehouse")
        );
        for (Class<?> type : entities) createTable(type);
    }

    private Class<?> entity(String name) {
        try { return Class.forName("com.foodtraceability.enterprise.entity." + name); }
        catch (ClassNotFoundException e) { throw new IllegalStateException(e); }
    }

    private void createTable(Class<?> type) {
        String table = type.getAnnotation(TableName.class).value();
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(table).append(" (");
        Field[] fields = type.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String column = "enterpriseId".equals(field.getName()) ? "enterprise_uuid" : snake(field.getName());
            sql.append(column).append(' ').append(columnType(field));
            if (i < fields.length - 1) sql.append(", ");
        }
        sql.append(')');
        jdbc.execute(sql.toString());
    }

    private String columnType(Field field) {
        if (field.getName().endsWith("Id") && field.getType() == Long.class) return "BIGINT AUTO_INCREMENT";
        if (field.getType() == Integer.class || field.getType() == int.class) return "INT DEFAULT 0";
        if (field.getType() == BigDecimal.class) return "DECIMAL(18,2)";
        return "VARCHAR(512)";
    }

    private String snake(String name) { return name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(); }
}

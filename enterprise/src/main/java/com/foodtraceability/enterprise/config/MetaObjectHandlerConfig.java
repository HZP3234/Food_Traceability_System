package com.foodtraceability.enterprise.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MyBatis-Plus 自动填充处理器（统一处理所有模块）
 * <p>
 * INSERT 时自动填充 create_by、create_time、update_by、update_time<br>
 * UPDATE 时自动填充 update_by、update_time
 * <p>
 * 兼容 String 和 LocalDateTime 两种字段类型：
 * - enterprise 模块实体使用 String（格式化时间）
 * - customers 模块实体使用 LocalDateTime
 */
@Component
public class MetaObjectHandlerConfig implements MetaObjectHandler {

    private static final Logger log = LoggerFactory.getLogger(MetaObjectHandlerConfig.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_OPERATOR = "SYSTEM";

    @PostConstruct
    public void init() {
        log.info("MetaObjectHandlerConfig 已注册 — 自动填充 create_by/create_time/update_by/update_time（兼容 String / LocalDateTime）");
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        log.debug("insertFill, metaObject={}", metaObject.getOriginalObject().getClass().getSimpleName());

        fillTimeField(metaObject, "createTime", now);
        fillTimeField(metaObject, "updateTime", now);
        setIfHasField(metaObject, "createBy", DEFAULT_OPERATOR);
        setIfHasField(metaObject, "updateBy", DEFAULT_OPERATOR);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        log.debug("updateFill, metaObject={}", metaObject.getOriginalObject().getClass().getSimpleName());

        fillTimeField(metaObject, "updateTime", now);
        setIfHasField(metaObject, "updateBy", DEFAULT_OPERATOR);
    }

    /**
     * 根据字段类型自动填充时间值：String → 格式化字符串，LocalDateTime → 原生类型
     */
    private void fillTimeField(MetaObject metaObject, String fieldName, LocalDateTime value) {
        if (!metaObject.hasSetter(fieldName)) return;
        Class<?> setterType = metaObject.getSetterType(fieldName);
        if (setterType == null) return;

        if (LocalDateTime.class.isAssignableFrom(setterType)) {
            this.setFieldValByName(fieldName, value, metaObject);
        } else if (String.class.isAssignableFrom(setterType)) {
            this.setFieldValByName(fieldName, value.format(DATE_FMT), metaObject);
        }
        // 其他类型跳过，避免类型转换异常
    }

    private void setIfHasField(MetaObject metaObject, String fieldName, Object value) {
        if (metaObject.hasSetter(fieldName)) {
            this.setFieldValByName(fieldName, value, metaObject);
        }
    }
}

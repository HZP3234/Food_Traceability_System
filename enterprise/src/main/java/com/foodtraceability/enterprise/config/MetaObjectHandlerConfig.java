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
 * MyBatis-Plus 自动填充处理器
 * <p>
 * INSERT 时自动填充 create_by、create_time、update_by、update_time<br>
 * UPDATE 时自动填充 update_by、update_time
 * <p>
 * 使用 setFieldValByName 直接按字段名填充，无需 @TableField 注解。
 */
@Component
public class MetaObjectHandlerConfig implements MetaObjectHandler {

    private static final Logger log = LoggerFactory.getLogger(MetaObjectHandlerConfig.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_OPERATOR = "SYSTEM";

    @PostConstruct
    public void init() {
        log.info("MetaObjectHandlerConfig 已注册为 Spring Bean — 自动填充 create_by/create_time/update_by/update_time");
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        String now = LocalDateTime.now().format(DATE_FMT);
        log.debug("insertFill 调用, metaObject={}", metaObject.getOriginalObject().getClass().getSimpleName());
        // 使用 setFieldValByName 而非 strictInsertFill，避免强依赖 @TableField 注解
        this.setFieldValByName("createTime", now, metaObject);
        this.setFieldValByName("updateTime", now, metaObject);
        this.setFieldValByName("createBy", DEFAULT_OPERATOR, metaObject);
        this.setFieldValByName("updateBy", DEFAULT_OPERATOR, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String now = LocalDateTime.now().format(DATE_FMT);
        log.debug("updateFill 调用, metaObject={}", metaObject.getOriginalObject().getClass().getSimpleName());
        this.setFieldValByName("updateTime", now, metaObject);
        this.setFieldValByName("updateBy", DEFAULT_OPERATOR, metaObject);
    }
}

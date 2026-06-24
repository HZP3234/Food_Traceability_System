package com.foodtraceability.regulation.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 插件配置
 * <p>
 * 注：SqlSessionFactory 已由 mybatis-plus-spring-boot3-starter 自动配置。
 * 分页插件仅在容器中不存在 MybatisPlusInterceptor 时才创建（避免与其他模块冲突）。
 */
@Configuration
public class RegulationMyBatisPlusConfig {

    @Bean
    @ConditionalOnMissingBean(MybatisPlusInterceptor.class)
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }
}

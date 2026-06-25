package com.foodtraceability.regulation.config;

import com.foodtraceability.regulation.security.QualificationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置 — 注册资质审核拦截器
 */
@Configuration
@RequiredArgsConstructor
public class QualificationWebMvcConfig implements WebMvcConfigurer {

    private final QualificationInterceptor qualificationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(qualificationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/qualification/**", "/api/enterprise/**");
    }
}
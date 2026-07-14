package com.miniproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableAsync
public class WebMvcConfig implements WebMvcConfigurer {

    private final MenuAccessLogInterceptor menuAccessLogInterceptor;

    public WebMvcConfig(MenuAccessLogInterceptor menuAccessLogInterceptor) {
        this.menuAccessLogInterceptor = menuAccessLogInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(menuAccessLogInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/health",
                        "/api/settings/public"
                );
    }
}

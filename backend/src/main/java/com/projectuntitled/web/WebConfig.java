package com.projectuntitled.web;

import com.projectuntitled.auth.AuthPaths;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;
    private final CurrentAccountArgumentResolver currentAccountArgumentResolver;

    public WebConfig(TenantInterceptor tenantInterceptor, CurrentAccountArgumentResolver currentAccountArgumentResolver) {
        this.tenantInterceptor = tenantInterceptor;
        this.currentAccountArgumentResolver = currentAccountArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor).addPathPatterns(AuthPaths.MEMBER + "/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentAccountArgumentResolver);
    }
}

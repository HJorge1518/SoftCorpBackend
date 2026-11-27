package com.softcorp.SoftCorpBackend.config;

import com.softcorp.SoftCorpBackend.auditoria.AuditoriaInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuditoriaInterceptor auditoriaInterceptor;

    public WebConfig(AuditoriaInterceptor auditoriaInterceptor) {
        this.auditoriaInterceptor = auditoriaInterceptor;
    }

    // Intercepta únicamente rutas /api/**, excluyendo monitoreo (Actuator) y documentación (Swagger)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditoriaInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/actuator/**", "/docs/**",
                        "/swagger-ui/**", "/v3/api-docs/**");
    }
}

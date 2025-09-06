package com.medwiz.novare_crm.config;
import com.medwiz.novare_crm.interceptor.SessionValidationInterceptor;
import com.medwiz.novare_crm.interceptor.UserIdInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SessionValidationInterceptor sessionValidationInterceptor;
    private final UserIdInterceptor userIdInterceptor;

    // private final FileStorageProperties props;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userIdInterceptor)
                .addPathPatterns("/api/**");
        registry.addInterceptor(sessionValidationInterceptor)
                .addPathPatterns("/api/**") // Apply to all protected APIs
                .excludePathPatterns(
                        "/api/v1/auth/**",     // exclude login, register, otp
                        "/api/v1/home/**",
                        "/swagger-ui/**",      // optional: exclude Swagger
                        "/v3/api-docs/**",     // optional: exclude Swagger docs
                        "/actuator/**",        // optional: exclude health checks
                        "/public/**"           // optional: exclude public APIs
                );
    }


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/auth/**")
                .allowedOrigins("*")
                .allowedMethods("GET")
                .allowedHeaders("Range", "Content-Type")
                .exposedHeaders("Content-Range", "Accept-Ranges", "Content-Length");
    }
}

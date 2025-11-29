package cc.misononoa.nishibi.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import cc.misononoa.nishibi.web.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor limiter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(limiter);
    }

    @ConfigurationProperties(prefix = "nishibi.web.rate-limit")
    public static record RateLimitProperties(
            int window,
            int limit) {
    }

}

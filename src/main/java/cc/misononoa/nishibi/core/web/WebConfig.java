package cc.misononoa.nishibi.core.web;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;

import cc.misononoa.nishibi.core.web.interceptor.RateLimitInterceptor;
import cc.misononoa.nishibi.core.web.view.NishibiMdProcessor;
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

    @Bean
    NishibiDialect nishibiDialect() {
        return new NishibiDialect();
    }

    private static class NishibiDialect extends AbstractProcessorDialect {

        NishibiDialect() {
            super("NishibiDialect", "nishibi", 1000);
        }

        @Override
        public Set<IProcessor> getProcessors(String dialectPrefix) {
            return Set.of(new NishibiMdProcessor(dialectPrefix));
        }

    }

}

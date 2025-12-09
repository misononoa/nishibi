package cc.misononoa.nishibi.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.RememberMeConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequestHeader;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @ConfigurationProperties(prefix = "nishibi.web.cors")
    public static record CorsConfigurationProperties(
            List<String> allowedOrigins) {
    }

    private final CorsConfigurationProperties corsConfigurationProperties;

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity httpSecurity,
            @Qualifier("nishibi-customized") CorsConfigurationSource corsConfigurationSource) {
        final var cspDirectives = "script-src 'self' 'sha256-PywGR6ofLvqaqa9FvJYmWwHVW+dkKubUi+wD5MUKkmE=' https://unpkg.com/;";
        return httpSecurity
                .httpBasic(HttpBasicConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .logout(LogoutConfigurer::disable)
                .rememberMe(RememberMeConfigurer::disable)
                .authorizeHttpRequests(t -> t.anyRequest().permitAll())
                .cors(t -> t.configurationSource(corsConfigurationSource))
                .headers(headers -> headers
                        .frameOptions(FrameOptionsConfig::sameOrigin)
                        .contentSecurityPolicy(csp -> csp.policyDirectives(cspDirectives))
                        .httpStrictTransportSecurity(hsts -> hsts.preload(true)))
                .build();
    }

    @Bean("nishibi-customized")
    CorsConfigurationSource corsConfigurationSource() {
        final var config = new CorsConfiguration();
        if (corsConfigurationProperties.allowedOrigins() instanceof List<String> allowedOrigins) {
            config.setAllowedOrigins(allowedOrigins);
        }
        config.setAllowedMethods(Stream.of(HttpMethod.values()).map(Object::toString).toList());
        final var allowedHeaders = new ArrayList<String>(20);
        allowedHeaders.addAll(List.of("Content-Type", "Accept", "X-CSRF-TOKEN"));
        for (HtmxRequestHeader header : HtmxRequestHeader.values())
            allowedHeaders.add(header.getValue());
        config.setAllowedHeaders(allowedHeaders);
        final var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
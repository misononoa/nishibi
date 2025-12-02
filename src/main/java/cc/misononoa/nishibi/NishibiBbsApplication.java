package cc.misononoa.nishibi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.RememberMeConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@ConfigurationPropertiesScan
@SpringBootApplication
public class NishibiBbsApplication {

	public static void main(String[] args) {
		SpringApplication.run(NishibiBbsApplication.class, args);
	}

	@Configuration
	@EnableWebSecurity
	public static class SecurityConfig {

		@Bean
		SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
			return httpSecurity
					.httpBasic(HttpBasicConfigurer::disable)
					.formLogin(FormLoginConfigurer::disable)
					.logout(LogoutConfigurer::disable)
					.rememberMe(RememberMeConfigurer::disable)
					.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
					.cors(Customizer.withDefaults())
					.headers(headers -> {
						headers
								.frameOptions(FrameOptionsConfig::sameOrigin)
								.httpStrictTransportSecurity(hsts -> hsts.preload(true));
					})
					.build();
		}

	}

}

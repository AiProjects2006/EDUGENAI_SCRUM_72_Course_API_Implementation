package com.ailearning.courseservice.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Value("${app.security.open-course-api:true}")
    private boolean openCourseApi;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/actuator/**",
                            "/error").permitAll();

                    auth.requestMatchers(
                            HttpMethod.OPTIONS,
                            "/**").permitAll();

                    if (openCourseApi) {
                        auth.requestMatchers("/api/v1/**").permitAll();
                    }

                    auth.anyRequest().authenticated();
                });

        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(
                Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(
                List.of("http://localhost:5173", "http://localhost:5174", "http://localhost:5175"));

        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE",
                        "PATCH", "OPTIONS"));

        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
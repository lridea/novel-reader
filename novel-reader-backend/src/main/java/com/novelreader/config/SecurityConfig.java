package com.novelreader.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 安全过滤器链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF
                .csrf(csrf -> csrf.disable())

                // 配置CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 配置会话管理（无状态）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置授权规则
                .authorizeHttpRequests(auth -> auth
                        // 静态资源（无需认证）
                        .requestMatchers("/uploads/**").permitAll()

                        // 公开接口（无需认证）
                        .requestMatchers(
                                "/api/crawler/health",
                                "/api/crawler/novels/**",
                                "/api/novels/page",
                                "/api/novels/tags",
                                "/api/novels/{id}",
                                "/api/comments/novel/**",
                                "/api/favorites/check-batch"
                        ).permitAll()

                        // 认证接口（无需认证）
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // 爬虫管理接口（需要ADMIN角色）
                        .requestMatchers(
                                "/api/crawler/crawlers",
                                "/api/crawler/trigger",
                                "/api/crawler/trigger/**",
                                "/api/crawler/test/**",
                                "/api/crawler/status/**",
                                "/api/crawler/configs",
                                "/api/crawler/tasks/**"
                        ).hasRole("ADMIN")

                        // 用户相关接口（需要USER或ADMIN角色）
                        .requestMatchers(
                                "/api/favorites/**",
                                "/api/categories/**",
                                "/api/users/**",
                                "/api/auth/me",
                                "/api/auth/logout",
                                "/api/comments/*/like",
                                "/api/upload/**"
                        ).hasAnyRole("USER", "ADMIN")

                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )

                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

package com.example.common;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;



@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Resource
    private CustomUserDetailsService customUserDetailsService;

    @Resource
    private CorsFilter corsFilter;

    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        http.addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/403", "/404").permitAll() // 放行403页面
                .requestMatchers("/files/**", "/upload/**", "/static/**").permitAll()
                .requestMatchers("/user/import", "/user/export", "/academy/selectAll").permitAll()
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SP_ADMIN")
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN", "SP_USER", "SP_ADMIN")
                .anyRequest().authenticated()
        );

        http.exceptionHandling(ex -> ex
                // 1. 未认证异常（401）
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=utf-8");
                    response.getWriter().write(JSON.toJSONString(Result.error("401", "未登录或Token失效")));
                })
                // 2. 权限不足异常（403）
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=utf-8");
                    response.getWriter().write(JSON.toJSONString(Result.error("403", "权限不足，无法访问")));
                    response.setStatus(403);
                })
        );

        return http.build();
    }
}
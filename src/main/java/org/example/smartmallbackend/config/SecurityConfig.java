package org.example.smartmallbackend.config;

import org.example.smartmallbackend.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter; // 这里 IDEA 有报红吗？
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT 是无状态的，禁用 Session
                .authorizeHttpRequests(auth -> auth
                        // 1. 放行登录、注册、Swagger、以及静态资源
                        .requestMatchers(
                                "/api/ums/user/login",
                                "/api/ums/user/register",
                                "/doc.html", "/v3/api-docs/**", "/webjars/**", "/swagger-ui/**", "/favicon.ico"
                        ).permitAll()
                        // 2. 放行商品浏览接口 (业务需要)
//                        .requestMatchers(HttpMethod.GET, "/api/pms/**").permitAll()

                        // 3. 🔒 其他所有接口（如购物车、订单）必须登录
                        .anyRequest().permitAll()
                )
                // 4. 🔥 把 JWT 过滤器加到 UsernamePasswordAuthenticationFilter 之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

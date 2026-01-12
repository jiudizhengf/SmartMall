package org.example.smartmallbackend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.smartmallbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 获取 Authorization 头
        String authHeader = request.getHeader("Authorization");
        // 2. 判断是否存在且格式正确 (Bearer xxx)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // 去掉 "Bearer "
            // 3. 校验 Token 有效性
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserId(token);
                // 实际项目中可能还需要查数据库获取用户权限 (UserDetails)
                // 这里为了简单，直接构造一个已认证的 Authentication 对象

                // 主要目的是把 userId 存进去，SecurityContextHolder 全局可用
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.emptyList()); // 暂无权限集合

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 4.告诉 Spring Security 当前用户已认证
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);

    }
}

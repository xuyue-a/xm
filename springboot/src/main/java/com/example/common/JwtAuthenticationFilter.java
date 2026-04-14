package com.example.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.utils.TokenUtils;
import cn.hutool.core.util.StrUtil;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * 每次请求执行：解析JWT Token，设置认证信息
     * 优化：直接从Request提取Token，调用TokenUtils重载方法，避免请求上下文依赖
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 直接从Request提取Token（不依赖RequestContextHolder）
            String token = request.getHeader("token");
            if (StrUtil.isBlank(token)) {
                token = request.getParameter("token");
            }

            // 2. 先通过Token获取用户，再加载UserDetails（避免嵌套异常）
            com.example.entity.Account account = TokenUtils.getCurrentUser(token);
            if (account != null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(account.getUsername());
                if (userDetails != null) {
                    // 3. 创建Spring Security认证令牌
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    // 4. 设置请求详情
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // 5. 将认证信息存入上下文
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (Exception e) {
            // Token无效时，仅打印警告，不阻断请求链
            logger.warn("JWT Token解析失败或用户未登录：", e);
        }

        // 6. 执行后续过滤器
        filterChain.doFilter(request, response);
    }
}
package com.example.common;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.entity.Account;
import com.example.exception.CustomException;
import com.example.service.AdminService;
import com.example.service.UserTableService;
import com.example.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component // 由Spring容器管理
public class JWTInterceptor implements HandlerInterceptor {

    @Autowired // 自动注入Spring管理的Service
    private AdminService adminService;
    @Autowired
    private UserTableService userTableService;

    // 定义需要放行的接口路径
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/login", "/register", "/files/upload", "/upload/**", "/static/**", "/user/import", "/user/export"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        if (isExcludePath(requestUri)) {
            return true;
        }

        // 复用TokenUtils获取用户，减少冗余逻辑
        Account account = TokenUtils.getCurrentUser();
        if (account == null) {
            throw new CustomException("401", "您无权限操作，请先登录");
        }

        // 签名验证（复用现有逻辑）
        String token = request.getHeader("token");
        if (StrUtil.isEmpty(token)) {
            token = request.getParameter("token");
        }
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(account.getPassword())).build();
            jwtVerifier.verify(token);
        } catch (Exception e) {
            throw new CustomException("401", "Token签名验证失败，您无权限操作");
        }

        return true;
    }

    private boolean isExcludePath(String requestUri) {
        for (String excludePath : EXCLUDE_PATHS) {
            if (excludePath.endsWith("/**")) {
                String prefix = excludePath.substring(0, excludePath.length() - 3);
                if (requestUri.startsWith(prefix)) {
                    return true;
                }
            } else {
                if (requestUri.equals(excludePath)) {
                    return true;
                }
            }
        }
        return false;
    }
}
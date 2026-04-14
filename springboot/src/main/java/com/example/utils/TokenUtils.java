package com.example.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.entity.Account;
import com.example.service.AdminService;
import com.example.service.UserTableService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Component
public class TokenUtils {

    @Resource
    AdminService adminService;
    @Resource
    UserTableService userTableService;

    static AdminService staticAdminService;
    static UserTableService staticUserService;

    // springboot工程启动后会加载这段代码
    @PostConstruct
    public void init() {
        staticAdminService = adminService;
        staticUserService = userTableService;
    }

    /**
     * 生成token
     */
    public static String createToken(String data, String sign) {
        return JWT.create().withAudience(data) // 将 userId-role 保存到 token 里面,作为载荷
                .withExpiresAt(DateUtil.offsetDay(new Date(), 1)) // 1天后token过期
                .sign(Algorithm.HMAC256(sign)); // 以 password 作为 token 的密钥, HMAC256算法加密
    }

    /**
     * 获取当前登录的用户信息
     * 增加请求上下文判空，避免空指针异常
     */
    public static Account getCurrentUser() {
        try {
            // 1. 先判空请求上下文，避免获取不到Request导致异常
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) {
                return null;
            }
            HttpServletRequest request = requestAttributes.getRequest();
            if (request == null) {
                return null;
            }

            // 2. 获取Token（请求头 → 请求参数）
            String token = request.getHeader("token");
            if (StrUtil.isBlank(token)) {
                token = request.getParameter("token");
            }
            if (StrUtil.isBlank(token)) {
                return null;
            }

            // 3. 解析token载荷
            String audience = JWT.decode(token).getAudience().get(0);
            String[] split = audience.split("-");
            // 增加数组长度判空，避免数组越界
            if (split.length != 2) {
                return null;
            }
            Integer userId = Integer.valueOf(split[0]);
            String role = split[1];

            // 4. 支持所有角色类型（ADMIN/SP_ADMIN/USER/SP_USER）
            if ("ADMIN".equals(role) || "SP_ADMIN".equals(role)) {
                return staticAdminService.selectById(userId);
            } else if ("USER".equals(role) || "SP_USER".equals(role)) {
                return staticUserService.selectById(userId);
            }
        } catch (Exception e) {
            // 异常时不打印堆栈（避免冗余日志），直接返回null
            return null;
        }
        return null;
    }

    /**
     * 重载方法：直接传入Token解析用户，供JwtAuthenticationFilter调用，避免依赖请求上下文
     */
    public static Account getCurrentUser(String token) {
        try {
            if (StrUtil.isBlank(token)) {
                return null;
            }
            // 解析token载荷
            String audience = JWT.decode(token).getAudience().get(0);
            String[] split = audience.split("-");
            if (split.length != 2) {
                return null;
            }
            Integer userId = Integer.valueOf(split[0]);
            String role = split[1];

            // 支持所有角色类型
            if ("ADMIN".equals(role) || "SP_ADMIN".equals(role)) {
                return staticAdminService.selectById(userId);
            } else if ("USER".equals(role) || "SP_USER".equals(role)) {
                return staticUserService.selectById(userId);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

}
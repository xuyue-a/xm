package com.example.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired // 注入Spring容器管理的JWTInterceptor
    private JWTInterceptor jwtInterceptor;

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 拦截所有路径
                .addPathPatterns("/**")
                // 排除不需要JWT校验的路径
                .excludePathPatterns(
                        // 登录注册
                        "/login", "/register",
                        // 文件上传
                        "/files/upload",
                        // 静态资源
                        "/files/**", "/upload/**", "/static/**",
                        // 导入导出
                        "/user/import", "/user/export",
                        // 学院查询
                        "/academy/selectAll"
                );
    }

    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String projectPath = System.getProperty("user.dir");
        String filesPath = projectPath + File.separator + "files" + File.separator;
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + filesPath);
    }

}
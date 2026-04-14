package com.example.common;

import com.example.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(CustomException.class)
    public Result handleCustomException(CustomException e, HttpServletRequest request) {
        String code = e.getCode();
        String msg = e.getMsg();

        // 获取当前请求路径
        String requestURI = request.getRequestURI();

        // 核心修改：扩展排除范围，新增 /user/add 和 /admin/add 接口
        // 只对 非注册、非用户新增、非管理员新增 接口的账号/密码异常统一提示“账号或密码错误”
        if (!"/register".equals(requestURI)
                && !"/user/add".equals(requestURI)
                && !"/admin/add".equals(requestURI)
                && "500".equals(code)
                && msg != null
                && (msg.contains("账号") || msg.contains("密码") || msg.contains("用户") || msg.contains("不存在"))) {
            return Result.error("500", "账号或密码错误");
        }

        // 注册、用户新增、管理员新增 接口直接返回原始异常信息（账号已存在）
        return Result.error(code, msg);
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletRequest request) {
        e.printStackTrace(); // 打印错误堆栈，便于调试

        String requestURI = request.getRequestURI();
        // 核心修改：排除注册、用户新增、管理员新增 接口，避免这些接口的异常被修改为登录错误提示
        if (!"/register".equals(requestURI)
                && !"/user/add".equals(requestURI)
                && !"/admin/add".equals(requestURI)) {
            // 如果是RuntimeException，可能包含有用的信息
            if (e instanceof RuntimeException) {
                String message = e.getMessage();
                if (message != null && (message.contains("账号") || message.contains("密码"))) {
                    return Result.error("500", "账号或密码错误");
                }
            }
        }

        // 其他系统异常/新增/注册接口异常，返回原始提示或系统异常
        return Result.error("500", "系统异常，请联系管理员");
    }
}
package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.UserTable;
import com.example.exception.CustomException;
import com.example.service.AdminService;
import com.example.service.UserTableService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
public class  WebController {

    @Resource
    private UserTableService userTableService;
    @Resource
    private AdminService adminService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello";
    }

    //    登录
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        try {
            Account dbAccount = null;
            if ("ADMIN".equals(account.getRole()) || "SP_ADMIN".equals(account.getRole())) {
                dbAccount = adminService.login(account);
            } else if ("USER".equals(account.getRole()) || "SP_USER".equals(account.getRole())) {
                dbAccount = userTableService.login(account);
            } else {
                throw new CustomException("500", "系统错误");
            }

            return Result.success(dbAccount);
        } catch (CustomException e) {
            // 让全局异常处理器处理
            throw e;
        } catch (Exception e) {
            // 其他异常也抛出
            throw new CustomException("500", "登录失败，请重试");
        }
    }

    //    注册
    @PostMapping("/register")
    public Result register(@RequestBody UserTable userTable) {
        userTableService.register(userTable);
        return Result.success();
    }

    //    修改密码
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if ("ADMIN".equals(account.getRole()) || "SP_ADMIN".equals(account.getRole())) {
            adminService.updatePassword(account);
        } else if ("USER".equals(account.getRole()) || "SP_USER".equals(account.getRole())) {
            userTableService.updatePassword(account);
        } else {
            throw new CustomException("500", "系统错误");
        }
        return Result.success(account);
    }


}
package com.example.common;

import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.UserTable;
import com.example.service.AdminService;
import com.example.service.UserTableService;
import com.example.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserTableService userTableService;

    /**
     * 加载用户信息（用户名+角色）
     * @param username 登录账号
     * @return UserDetails Spring Security标准用户信息
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 先查询管理员
        Admin admin = adminService.selectByUsername(username);
        if (admin != null) {
            // 角色必须以ROLE_开头，Spring Security默认识别该前缀
            String role = "ROLE_" + admin.getRole(); // 最终为ROLE_ADMIN/ROLE_SP_ADMIN
            return User.withUsername(admin.getUsername())
                    .password(admin.getPassword()) // 此处密码已加密，无需重复处理
                    .authorities(Collections.singletonList(() -> role)) // 授予角色权限
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        }

        // 再查询普通用户
        UserTable user = userTableService.selectByUsername(username);
        if (user != null) {
            String role = "ROLE_" + user.getRole(); // 最终为ROLE_USER/ROLE_SP_USER
            return User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .authorities(Collections.singletonList(() -> role))
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        }

        // 用户不存在
        throw new UsernameNotFoundException("账号不存在：" + username);
    }

    /**
     * 从当前登录用户（Token）加载UserDetails（兼容现有JWT逻辑）
     * 优化：增加判空，避免直接抛出异常
     * @return UserDetails
     */
    public UserDetails loadUserByCurrentToken() {
        Account account = TokenUtils.getCurrentUser();
        if (account == null) {
            throw new UsernameNotFoundException("当前用户未登录");
        }

        // 直接调用loadUserByUsername，复用逻辑，避免冗余
        return this.loadUserByUsername(account.getUsername());
    }
}
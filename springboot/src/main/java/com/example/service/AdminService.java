package com.example.service;

import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.exception.CustomException;
import com.example.mapper.AdminMapper;
import com.example.utils.PasswordUtils;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Resource
    private AdminMapper adminMapper;


    public void add(Admin admin) {
        String username = admin.getUsername();
        Admin dbAdmin = adminMapper.selectByUsername(username);
        if (dbAdmin != null) {
            throw new CustomException("500", "账号已存在");
        }

        // 密码处理：默认密码Admin123
        String rawPassword = StrUtil.isBlank(admin.getPassword()) ? "Admin123" : admin.getPassword();
        // 关键：先对明文密码做MD5（和前端登录时的加密一致）
        String md5Password = PasswordUtils.pureMD5(rawPassword); // 改用纯MD5方法
        // 再加盐二次加密（保证安全性）
        String salt = PasswordUtils.generateSalt();
        String finalPassword = PasswordUtils.encryptPassword(md5Password, salt);

        admin.setPassword(finalPassword);
        admin.setSalt(salt); // 保存盐值
        admin.setRole("ADMIN");
        if (StrUtil.isBlank(admin.getName())) {
            admin.setName(admin.getUsername());
        }
        adminMapper.insert(admin);
    }

    public void update(Admin admin) {
        adminMapper.updateById(admin);
    }

    public void deleteById(Integer id) {
        adminMapper.deleteById(id);
    }

    public List<Admin> selectAll(Admin admin) {
        return adminMapper.selectAll(admin);
    }

    public Admin selectById(Integer id) {
        return adminMapper.selectById(id);
    }

    public PageInfo<Admin> selectPage(Admin admin, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Admin> list = adminMapper.selectAll(admin);
        return PageInfo.of(list);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            this.deleteById(id);
        }
    }

    public Admin login(Account account) {
        String username = account.getUsername();
        Admin abAdmin = adminMapper.selectByUsername(username);
        if (abAdmin == null) {
            throw new CustomException("500", "该账号不存在");
        }


        // 验证逻辑：前端已传MD5加密后的密码，只需加盐二次加密后对比
        String encryptedPassword = PasswordUtils.encryptPassword(account.getPassword(), abAdmin.getSalt());
        if (!abAdmin.getPassword().equals(encryptedPassword)) {
            throw new CustomException("500", "账号或密码错误");
        }

        String token = TokenUtils.createToken(abAdmin.getId() + "-" + "ADMIN", abAdmin.getPassword());
        abAdmin.setToken(token);
        return abAdmin;
    }

    public void updatePassword(Account account) {
        Integer id = account.getId();
        Admin admin = this.selectById(id);

        // 验证原密码
        String encryptedOldPassword = PasswordUtils.encryptPassword(account.getPassword(), admin.getSalt());
        if (!admin.getPassword().equals(encryptedOldPassword)) {
            throw new CustomException("500", "原密码错误");
        }

        // 加密新密码
        String newSalt = PasswordUtils.generateSalt();
        String newEncryptedPassword = PasswordUtils.encryptPassword(account.getNewPassword(), newSalt);
        admin.setPassword(newEncryptedPassword);
        admin.setSalt(newSalt);

        adminMapper.updatePasswordAndSalt(admin);
    }

    public void resetPassword(Integer userId, String newPassword) {
        // 1. 查询用户是否存在
        Admin admin = adminMapper.selectById(userId);
        if (admin == null) {
            throw new CustomException("500", "用户不存在");
        }
        // 2. 前端传的是明文，先MD5再加盐
        String md5Password = PasswordUtils.pureMD5(newPassword);
        String newSalt = PasswordUtils.generateSalt();
        String encryptedPassword = PasswordUtils.encryptPassword(md5Password, newSalt);
        // 3. 更新密码和盐值
        admin.setPassword(encryptedPassword);
        admin.setSalt(newSalt);
        adminMapper.updatePasswordAndSalt(admin);
    }

    public Admin selectByUsername(String username) {
        return adminMapper.selectByUsername(username);
    }
}
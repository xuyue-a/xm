package com.example.service;

import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.UserTable;
import com.example.exception.CustomException;
import com.example.mapper.UserTableMapper;
import com.example.utils.PasswordUtils;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserTableService {

    @Resource
    private UserTableMapper userTableMapper;

    public void add(UserTable userTable) {
        String username = userTable.getUsername();
        UserTable dbUserTable = userTableMapper.selectByUsername(username);
        if (dbUserTable != null) {
            throw new CustomException("500", "账号已存在");
        }

        // 密码处理：默认密码123456
        String rawPassword = StrUtil.isBlank(userTable.getPassword()) ? "123456" : userTable.getPassword();
        // 关键：先对明文密码做MD5（和前端登录时的加密一致）
        String md5Password = PasswordUtils.pureMD5(rawPassword);
        // 4. 生成唯一盐值
        String salt = PasswordUtils.generateSalt();
        // 5. 第二层加密：MD5加盐二次加密，生成最终存储密文
        String finalPassword = PasswordUtils.encryptPassword(md5Password, salt);

        userTable.setPassword(finalPassword);
        userTable.setSalt(salt);
        userTable.setRole("USER");
        if (StrUtil.isBlank(userTable.getName())) {
            userTable.setName(userTable.getUsername());
        }
        userTableMapper.insert(userTable);
    }

    public void resetPassword(Integer userId, String newPassword) {
        // 1. 查询用户是否存在
        UserTable user = userTableMapper.selectById(userId);
        if (user == null) {
            throw new CustomException("500", "用户不存在");
        }
        // 2. 前端传的是明文，先MD5再加盐
        String md5Password = PasswordUtils.pureMD5(newPassword);
        String newSalt = PasswordUtils.generateSalt();
        String encryptedPassword = PasswordUtils.encryptPassword(md5Password, newSalt);
        // 3. 更新密码和盐值
        user.setPassword(encryptedPassword);
        user.setSalt(newSalt);
        userTableMapper.updatePasswordAndSalt(user);
    }

    public void update(UserTable userTable) {
        userTableMapper.updateById(userTable);
    }

    public void deleteById(Integer id) {
        userTableMapper.deleteById(id);
    }

    public List<UserTable> selectAll(UserTable userTable) {
        return userTableMapper.selectAll(userTable);
    }

    public UserTable selectById(Integer id) {
        return userTableMapper.selectById(id);
    }

    public PageInfo<UserTable> selectPage(UserTable userTable, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<UserTable> list = userTableMapper.selectAll(userTable);
        return PageInfo.of(list);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            this.deleteById(id);
        }
    }

    public UserTable login(Account account) {
        String username = account.getUsername();
        UserTable dbUserTable = userTableMapper.selectByUsername(username);
        if (dbUserTable == null) {
            throw new CustomException("500", "账号或密码错误");
        }


        // 验证逻辑：前端已传MD5加密后的密码，只需加盐二次加密后对比
        String encryptedPassword = PasswordUtils.encryptPassword(account.getPassword(), dbUserTable.getSalt());
        if (!dbUserTable.getPassword().equals(encryptedPassword)) {
            throw new CustomException("500", "账号或密码错误");
        }

        String token = TokenUtils.createToken(dbUserTable.getId() + "-" + "USER", dbUserTable.getPassword());
        dbUserTable.setToken(token);
        return dbUserTable;
    }

    public void register(UserTable userTable) {
        this.add(userTable);
    }

    public void updatePassword(Account account) {
        Integer id = account.getId();
        UserTable userTable = this.selectById(id);

        // 验证原密码
        String encryptedOldPassword = PasswordUtils.encryptPassword(account.getPassword(), userTable.getSalt());
        if (!userTable.getPassword().equals(encryptedOldPassword)) {
            throw new CustomException("500", "原密码错误");
        }

        // 加密新密码
        String newSalt = PasswordUtils.generateSalt();
        String newEncryptedPassword = PasswordUtils.encryptPassword(account.getNewPassword(), newSalt);
        userTable.setPassword(newEncryptedPassword);
        userTable.setSalt(newSalt);

        userTableMapper.updatePasswordAndSalt(userTable);
    }

    public UserTable selectByUsername(String username) {
        return userTableMapper.selectByUsername(username);
    }
}
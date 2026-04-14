package com.example.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.UserTable;
import com.example.exception.CustomException;
import com.example.service.AcademyService;
import com.example.service.UserTableService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserTableController {

    @Resource
    private UserTableService userTableService;
    @Resource
    private AcademyService academyService; // 需自行实现学院查询服务

    //    新增
    @PreAuthorize("hasAnyRole('SP_ADMIN')")
    @PostMapping("/add")
    public Result add(@RequestBody UserTable userTable) {
        userTableService.add(userTable);
        return Result.success();
    }

    //    更新
    @PreAuthorize("hasAnyRole('SP_ADMIN')")
    @PutMapping("/update")
    public Result update(@RequestBody UserTable userTable) {
        userTableService.update(userTable);
        return Result.success();
    }

    //    删除单个
    @PreAuthorize("hasAnyRole('SP_ADMIN')")
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id) {
        userTableService.deleteById(id);
        return Result.success();
    }

    //   批量删除数据
    @PreAuthorize("hasAnyRole('SP_ADMIN')")
    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        userTableService.deleteBatch(ids);
        return Result.success();
    }

    //查询所有
    @GetMapping("/selectAll")
    public Result selectAll(UserTable userTable){
        List<UserTable> list = userTableService.selectAll(userTable);
        return Result.success(list);
    }

//    查询单个
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id){
        UserTable userTable = userTableService.selectById(id);
        return Result.success(userTable);
    }

    @GetMapping("/selectPage")
    public Result selectPage(UserTable userTable,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<UserTable> pageInfo = userTableService.selectPage(userTable,pageNum, pageSize);
        return Result.success(pageInfo);
    }

    @PreAuthorize("hasAnyRole('SP_ADMIN')")
    @PostMapping("/resetPassword")
    public Result resetPassword(@RequestBody UserTable userTable) {
        // 1. 参数校验
        if (userTable.getId() == null || StrUtil.isBlank(userTable.getPassword())) {
            throw new CustomException("500", "用户ID或新密码不能为空");
        }
        // 2. 调用服务层重置密码
        userTableService.resetPassword(userTable.getId(), userTable.getPassword());
        return Result.success();
    }

    @PutMapping("/updateSelf")
    public Result updateSelf(@RequestBody UserTable userTable) {
        // 1. 获取当前登录普通用户信息（从Token中解析）
        Account currentAccount = TokenUtils.getCurrentUser();
        if (currentAccount == null || !(currentAccount instanceof UserTable)) {
            throw new CustomException("401", "未登录或登录信息失效");
        }
        UserTable currentUser = (UserTable) currentAccount;
        // 2. 强制绑定当前用户ID，防止恶意修改他人信息
        userTable.setId(currentUser.getId());
        // 3. 禁止修改敏感字段（账号、角色、密码、所属学院，这些字段不允许个人修改）
        userTable.setUsername(currentUser.getUsername());
        userTable.setRole(currentUser.getRole());
        userTable.setPassword(currentUser.getPassword());
        userTable.setSalt(currentUser.getSalt());
        userTable.setAcademyId(currentUser.getAcademyId());
        userTable.setToken(null);
        // 4. 调用服务层更新
        userTableService.update(userTable);
        currentUser.setName(userTable.getName());
        currentUser.setSex(userTable.getSex());
        currentUser.setAvatar(userTable.getAvatar());
        return Result.success();
    }

    //  导出excel ids: 1,2,3
    @PreAuthorize("hasAnyRole('SP_ADMIN')")
    @GetMapping("/export")
    public void export(UserTable userTable, HttpServletResponse response) throws Exception {
        String ids = userTable.getIds();
        if (StrUtil.isNotBlank(ids)) {
            String[] idsArr = ids.split(",");
            userTable.setIdsArr(idsArr);
        }
        // 1.拿到所以的用户数据
        List<UserTable> userTableList = userTableService.selectAll(userTable);
        // 2.构建 Excel
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);
        // 3.设置中文表头
        writer.addHeaderAlias("username", "账号");
        writer.addHeaderAlias("name", "姓名");
        writer.addHeaderAlias("posts", "职位");
        writer.addHeaderAlias("sex", "性别");
        writer.addHeaderAlias("academyName", "所属学院");
        // 默认的，未添加alias的属性也会写出，可以用以下方法排除
        writer.setOnlyAlias(true);
        // 4.写出数据到writer
        writer.write(userTableList, true);
        // 5.设置输出的文件名称  以及输出流的头信息
        //设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("用户信息", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        // 6.写出到输出流
        ServletOutputStream os = response.getOutputStream();
        writer.flush(os);
        writer.close();
    }

    // 导入excel
    @PreAuthorize("hasAnyRole('SP_ADMIN')")
    @PostMapping("/import")
    public Result importData(MultipartFile file) throws Exception {
        // 1.拿到数据流，构建 reader
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 2.读取excel里面的数据（映射别名）
        reader.addHeaderAlias("账号", "username");
        reader.addHeaderAlias("姓名", "name");
        reader.addHeaderAlias("职位", "posts");
        reader.addHeaderAlias("性别", "sex");
        reader.addHeaderAlias("所属学院", "academyName");
        List<UserTable> userTableList = reader.readAll(UserTable.class);

        // 3.关键：遍历数据，通过学院名称获取academyId并设置
        for (UserTable userTable : userTableList) {
            if (StrUtil.isNotBlank(userTable.getAcademyName())) {
                // 调用学院服务，根据名称查询学院ID（需实现AcademyService的findIdByName方法）
                Integer academyId = academyService.findIdByName(userTable.getAcademyName());
                userTable.setAcademyId(academyId); // 设置学院ID
                // 清空academyName（若表中无该字段，可忽略）
                userTable.setAcademyName(null);
            }
            // 4.写入数据库（若有重复账号校验，可在此添加）
            userTableService.add(userTable);
        }
        return Result.success();
    }

}

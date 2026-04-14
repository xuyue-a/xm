package com.example.controller;


import com.example.common.Result;
import com.example.entity.Academy;
import com.example.service.AcademyService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/academy")
public class AcademyController {

    @Resource
    private AcademyService academyService;

    //    新增
    @PreAuthorize("hasAnyRole('SP_ADMIN')")
    @PostMapping("/add")
    public Result add(@RequestBody Academy academy) {
        academyService.add(academy);
        return Result.success();
    }

    //    更新
    @PreAuthorize("hasAnyRole('SP_ADMIN')")
    @PutMapping("/update")
    public Result update(@RequestBody Academy academy) {
        academyService.update(academy);
        return Result.success();
    }

    //    删除单个
    @PreAuthorize("hasAnyRole('SP_ADMIN')")
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id) {
        academyService.deleteById(id);
        return Result.success();
    }

    //   批量删除数据
    @PreAuthorize("hasAnyRole('SP_ADMIN')")
    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        academyService.deleteBatch(ids);
        return Result.success();
    }

    //查询所有
    @GetMapping("/selectAll")
    public Result selectAll(Academy academy){
        List<Academy> list = academyService.selectAll(academy);
        return Result.success(list);
    }

//    查询单个
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id){
        Academy academy = academyService.selectById(id);
        return Result.success(academy);
    }

    @GetMapping("/selectPage")
    public Result selectPage(Academy academy,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Academy> pageInfo = academyService.selectPage(academy,pageNum, pageSize);
        return Result.success(pageInfo);
    }

}

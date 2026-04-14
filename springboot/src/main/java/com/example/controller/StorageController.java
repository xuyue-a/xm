package com.example.controller;


import com.example.common.Result;
import com.example.entity.Storage;
import com.example.service.StorageService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/storage")
public class StorageController {

    @Resource
    private StorageService storageService;

    //    新增
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/add")
    public Result add(@RequestBody Storage storage) {
        storageService.add(storage);
        return Result.success();
    }

    //    更新
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/update")
    public Result update(@RequestBody Storage storage) {
        storageService.update(storage);
        return Result.success();
    }

    //    删除单个
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id) {
        storageService.deleteById(id);
        return Result.success();
    }

    //   批量删除数据
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        storageService.deleteBatch(ids);
        return Result.success();
    }

    //查询所有
    @GetMapping("/selectAll")
    public Result selectAll(Storage storage){
        List<Storage> list = storageService.selectAll(storage);
        return Result.success(list);
    }

    //    查询单个
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id){
        Storage storage = storageService.selectById(id);
        return Result.success(storage);
    }

    @GetMapping("/selectPage")
    public Result selectPage(Storage storage,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Storage> pageInfo = storageService.selectPage(storage,pageNum, pageSize);
        return Result.success(pageInfo);
    }


}

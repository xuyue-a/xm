package com.example.controller;


import com.example.common.Result;
import com.example.entity.ItemType;
import com.example.service.ItemTypeService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/itemType")
public class ItemTypeController {

    @Resource
    private ItemTypeService itemTypeService;

    //    新增
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/add")
    public Result add(@RequestBody ItemType itemType) {
        try {
            itemTypeService.add(itemType);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 新增：检查分类代码是否存在
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/checkTypeCode/{typeCode}")
    public Result<Boolean> checkTypeCode(@PathVariable String typeCode) {
        return Result.success(itemTypeService.checkTypeCodeExist(typeCode));
    }

    // 编辑
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/update")
    public Result update(@RequestBody ItemType itemType) {
        try {
            itemTypeService.update(itemType);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    //    删除单个
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id) {
        itemTypeService.deleteById(id);
        return Result.success();
    }

    //   批量删除数据
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        itemTypeService.deleteBatch(ids);
        return Result.success();
    }

    //查询所有
    @GetMapping("/selectAll")
    public Result selectAll(ItemType itemType){
        List<ItemType> list = itemTypeService.selectAll(itemType);
        return Result.success(list);
    }

    //    查询单个
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id){
        ItemType itemType = itemTypeService.selectById(id);
        return Result.success(itemType);
    }

    @GetMapping("/selectPage")
    public Result selectPage(ItemType itemType,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<ItemType> pageInfo = itemTypeService.selectPage(itemType,pageNum, pageSize);
        return Result.success(pageInfo);
    }

}

package com.example.controller;

import com.example.common.Result;
import com.example.entity.Item;
import com.example.service.ItemService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/item")
public class ItemController {
    @Resource
    private ItemService itemService;

    //    新增
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/add")
    public Result add(@RequestBody Item item) {
        try {
            itemService.add(item);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    //    更新
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/update")
    public Result update(@RequestBody Item item) {
        try {
            itemService.update(item);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    //    删除单个
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id) {
        itemService.deleteById(id);
        return Result.success();
    }

    //   批量删除数据
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        itemService.deleteBatch(ids);
        return Result.success();
    }

    //查询所有
    @GetMapping("/selectAll")
    public Result selectAll(Item item){
        List<Item> list = itemService.selectAll(item);
        return Result.success(list);
    }

    //    查询单个
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id){
        Item item = itemService.selectById(id);
        return Result.success(item);
    }

    // 新增：根据物品ID查询库存
    @GetMapping("/getCount/{itemId}")
    public Result<Integer> getCount(@PathVariable Integer itemId) {
        Item item = itemService.selectById(itemId);
        if (item == null) {
            return Result.error("物品不存在");
        }
        Integer count = item.getCount() == null ? 0 : item.getCount();
        return Result.success(count);
    }

    /**
     * 新增接口：按物品名称+型号查询库存（核心：实现三级联动后查库存）
     */
    @GetMapping("/getCount/byNameAndModel")
    public Result<Integer> getItemCountByNameAndModel(@RequestParam String itemName, @RequestParam String model) {
        try {
            List<Item> itemList = itemService.selectByName(itemName);
            Item targetItem = itemList.stream()
                    .filter(item -> model.equals(item.getModel()))
                    .findFirst()
                    .orElse(null);
            return Result.success(targetItem == null ? 0 : targetItem.getCount());
        } catch (Exception e) {
            return Result.error("查询库存失败：" + e.getMessage());
        }
    }

    @GetMapping("/selectPage")
    public Result selectPage(Item item,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Item> pageInfo = itemService.selectPage(item,pageNum, pageSize);
        return Result.success(pageInfo);
    }




}

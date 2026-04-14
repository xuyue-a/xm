package com.example.service;
import com.example.entity.Item;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface ItemService {
    void add(Item item);

    void update(Item item);

    void deleteById(Integer id);

//    List<Item> selectAll(Item item);

    Item selectById(Integer id);

    PageInfo<Item> selectPage(Item item, Integer pageNum, Integer pageSize);

    void deleteBatch(List<Integer> ids);

    // 原有带条件查询
    List<Item> selectAll(Item item);

    // 新增：根据分类ID查询物品
    List<Item> selectByTypeId(Integer typeId);
    // 新增：查询所有物品（无参，用于下拉选择）
    List<Item> selectAll();
    List<Item> selectByName(String itemName);

    Item selectByNameAndModel(String name, String model);
}

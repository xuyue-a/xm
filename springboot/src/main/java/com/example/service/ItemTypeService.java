package com.example.service;

import com.example.entity.ItemType;
import com.github.pagehelper.PageInfo;
import java.util.List;

public interface ItemTypeService {
    void add(ItemType itemType);
    void update(ItemType itemType);
    void deleteById(Integer id);
    // 重载：查询所有物品分类（无参，用于下拉选择）
    List<ItemType> selectAll();
    // 重载：带条件查询物品分类（用于分页查询）
    List<ItemType> selectAll(ItemType itemType);
    ItemType selectById(Integer id);
    PageInfo<ItemType> selectPage(ItemType itemType, Integer pageNum, Integer pageSize);
    void deleteBatch(List<Integer> ids);
    // 新增：接口方法——根据名称精准查询物品分类
    ItemType selectByName(String name);
    // 新增：检查分类代码是否存在
    boolean checkTypeCodeExist(String typeCode);

    void insert(ItemType itemType);
}

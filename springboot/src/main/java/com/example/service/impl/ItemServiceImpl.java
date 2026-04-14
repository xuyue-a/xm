package com.example.service.impl;

import com.example.entity.Item;
import com.example.entity.ItemType;
import com.example.mapper.ItemMapper;
import com.example.service.ItemService;
import com.example.service.ItemTypeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    @Resource
    private ItemMapper itemMapper;
    @Resource
    private ItemTypeService itemTypeService;

    @Override
    public void add(Item item) {
        // 1. 自动生成物品代码：分类代码 + 001/002...
        if (item.getTypeId() == null) {
            throw new RuntimeException("物品分类不能为空");
        }
        ItemType type = itemTypeService.selectById(item.getTypeId());
        if (type == null || !StringUtils.hasText(type.getTypeCode())) {
            throw new RuntimeException("分类代码不存在，无法生成物品代码");
        }

        // 2. 查询分类下物品数量，生成3位序号
        int count = itemMapper.countByTypeId(item.getTypeId());
        DecimalFormat df = new DecimalFormat("000");
        String itemCode = type.getTypeCode() + df.format(count + 1);

        // 3. 设置物品代码并保存
        item.setItemCode(itemCode);
        itemMapper.insert(item);
    }

    @Override
    public void update(Item item) {
        // 编辑时物品代码不可修改（如需修改，需重新生成）
        Item oldItem = selectById(item.getId());
        item.setItemCode(oldItem.getItemCode());
        itemMapper.updateById(item);
    }

    @Override
    public void deleteById(Integer id) {
        itemMapper.deleteById(id);
    }

    // 重载：无参查询所有物品（用于下拉选择）
    @Override
    public List<Item> selectAll() {
        return itemMapper.selectAll(null);
    }

    @Override
    public List<Item> selectByName(String itemName) {
        // 非空校验：避免传入空字符串/空格执行无效SQL，返回空列表而非null，兼容前端处理
        if (!StringUtils.hasText(itemName)) {
            return List.of();
        }
        // 调用Mapper方法，返回同名物品列表，保持原有业务逻辑
        return itemMapper.selectByName(itemName);
    }

    // 原有带条件查询
    @Override
    public List<Item> selectAll(Item item) {
        return itemMapper.selectAll(item);
    }

    // 新增：根据分类ID查询物品
    @Override
    public List<Item> selectByTypeId(Integer typeId) {
        return itemMapper.selectByTypeId(typeId);
    }

    @Override
    public Item selectById(Integer id) {
        return itemMapper.selectById(id);
    }

    @Override
    public PageInfo<Item> selectPage(Item item, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Item> list = itemMapper.selectAll(item);
        return PageInfo.of(list);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        itemMapper.deleteBatch(ids);
    }

    // 新增：精准查询物品（名称+分类ID+仓库ID）
    public Item selectByNameAndTypeIdAndStorageId(String name, Integer typeId, Integer storageId) {
        return itemMapper.selectByNameAndTypeIdAndStorageId(name, typeId, storageId);
    }

    // 核心新增：精准查询物品（名称+分类ID+型号）
    public Item selectByNameAndTypeIdAndModel(String name, Integer typeId, String model) {
        return itemMapper.selectByNameAndTypeIdAndModel(name, typeId, model);
    }

    @Override
    public Item selectByNameAndModel(String itemName, String model) {
        if (itemName == null || model == null) {
            return null;
        }
        // 对应Mapper接口需新增该查询方法
        return itemMapper.selectByNameAndModel(itemName, model);
    }

}
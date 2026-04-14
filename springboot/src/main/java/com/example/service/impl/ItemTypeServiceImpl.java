package com.example.service.impl;

import com.example.entity.ItemType;
import com.example.mapper.ItemTypeMapper;
import com.example.service.ItemTypeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ItemTypeServiceImpl implements ItemTypeService {

    @Resource
    private ItemTypeMapper itemTypeMapper;

    @Override
    public void add(ItemType itemType) {
        // 分类代码非空校验 + 唯一性校验
        if (!StringUtils.hasText(itemType.getTypeCode())) {
            throw new RuntimeException("分类代码不能为空");
        }
        if (checkTypeCodeExist(itemType.getTypeCode())) {
            throw new RuntimeException("分类代码已存在");
        }
        itemTypeMapper.insert(itemType);
    }

    @Override
    public void update(ItemType itemType) {
        // 分类代码唯一性校验（排除自身）
        if (StringUtils.hasText(itemType.getTypeCode())) {
            ItemType oldType = selectById(itemType.getId());
            if (oldType == null) {
                throw new RuntimeException("分类不存在");
            }
            if (!oldType.getTypeCode().equals(itemType.getTypeCode()) && checkTypeCodeExist(itemType.getTypeCode())) {
                throw new RuntimeException("分类代码已存在");
            }
        }
        itemTypeMapper.updateById(itemType);
    }

    @Override
    public void deleteById(Integer id) {
        itemTypeMapper.deleteById(id);
    }

    // 无参查询所有物品分类（用于下拉选择）
    @Override
    public List<ItemType> selectAll() {
        return itemTypeMapper.selectAll(null);
    }

    // 带条件查询物品分类（用于分页查询）
    @Override
    public List<ItemType> selectAll(ItemType itemType) {
        return itemTypeMapper.selectAll(itemType);
    }

    @Override
    public ItemType selectById(Integer id) {
        return itemTypeMapper.selectById(id);
    }

    @Override
    public PageInfo<ItemType> selectPage(ItemType itemType, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ItemType> list = itemTypeMapper.selectAll(itemType);
        return PageInfo.of(list);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        itemTypeMapper.deleteBatch(ids);
    }

    @Override
    public boolean checkTypeCodeExist(String typeCode) {
        // 空代码直接返回存在（禁止空代码）
        if (!StringUtils.hasText(typeCode)) {
            return true;
        }
        return itemTypeMapper.checkTypeCodeExist(typeCode) > 0;
    }

    // 补全空实现的insert方法（对接Controller的新增接口）
    @Override
    public void insert(ItemType itemType) {
        // 复用add方法的校验逻辑，保证一致性
        add(itemType);
    }

    // 核心实现：根据名称精准查询物品分类
    @Override
    public ItemType selectByName(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        return itemTypeMapper.selectByName(name);
    }

}
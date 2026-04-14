package com.example.mapper;

import com.example.entity.ItemType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ItemTypeMapper {
    // 基础CRUD
    void insert(ItemType itemType);
    void updateById(ItemType itemType);

    @Delete("delete from `itemtype` where id = #{id}")
    void deleteById(Integer id);

    // 支持传入null（无参查询所有）
    List<ItemType> selectAll(ItemType itemType);
    ItemType selectById(Integer id);

    // 核心新增：根据名称精准查询分类
    @Select("select id, name, type_code as typeCode, remark from `itemtype` where name = #{name} limit 1")
    ItemType selectByName(String name);

    // 核心新增：校验分类代码唯一性
    @Select("select count(1) from `itemtype` where type_code = #{typeCode}")
    int checkTypeCodeExist(String typeCode);

    // 批量删除
    void deleteBatch(List<Integer> ids);
}
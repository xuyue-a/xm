package com.example.service;
import com.example.entity.Storage;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface StorageService {

    void add(Storage storage);

    void update(Storage storage);

    void deleteById(Integer id);

//    List<Storage> selectAll(Storage storage);

    // 原有带条件查询
    List<Storage> selectAll(Storage storage);

    Storage selectById(Integer id);

    PageInfo<Storage> selectPage(Storage storage, Integer pageNum, Integer pageSize);

    void deleteBatch(List<Integer> ids);

    // 新增：查询所有仓库（无参，用于下拉选择）
    List<Storage> selectAll();
}
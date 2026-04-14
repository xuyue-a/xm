package com.example.service.impl;

import com.example.entity.Storage;
import com.example.mapper.StorageMapper;
import com.example.service.StorageService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StorageServiceImpl implements StorageService {

    @Resource
    private StorageMapper storageMapper;

    @Override
    public void add(Storage storage) {
        storageMapper.insert(storage);
    }

    @Override
    public void update(Storage storage) {
        storageMapper.updateById(storage);
    }

    @Override
    public void deleteById(Integer id) {
        storageMapper.deleteById(id);
    }

    // 重载：无参查询所有仓库（用于下拉选择）
    @Override
    public List<Storage> selectAll() {
        return storageMapper.selectAll(null);
    }

    // 原有带条件查询
    @Override
    public List<Storage> selectAll(Storage storage) {
        return storageMapper.selectAll(storage);
    }

    @Override
    public Storage selectById(Integer id) {
        return storageMapper.selectById(id);
    }

    @Override
    public PageInfo<Storage> selectPage(Storage storage, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Storage> list = storageMapper.selectAll(storage);
        return PageInfo.of(list);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            this.deleteById(id);
        }
    }
}

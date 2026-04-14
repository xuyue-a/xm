package com.example.service.impl;

import com.example.entity.Equipment;
import com.example.mapper.EquipmentMapper;
import com.example.service.EquipmentService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备服务实现类
 */
@Service
public class EquipmentServiceImpl implements EquipmentService {

    @Resource
    private EquipmentMapper equipmentMapper;

    @Override
    public List<Equipment> selectAll(String name, String brand, String status, Integer storageId, Integer userId) {
        return equipmentMapper.selectAll(name, brand, status, storageId, userId);
    }

    @Override
    public boolean addEquipment(Equipment equipment) {
        return equipmentMapper.insert(equipment) > 0;
    }

    @Override
    public boolean updateEquipment(Equipment equipment) {
        return equipmentMapper.updateById(equipment) > 0;
    }

    @Override
    public boolean deleteEquipment(Integer id) {
        return equipmentMapper.deleteById(id) > 0;
    }

    @Override
    public Equipment selectById(Integer id) {
        return equipmentMapper.selectById(id);
    }

    @Override
    public List<Map<String, Object>> selectEquipStats(Integer equipId, String startTime, String endTime) {
        return equipmentMapper.selectEquipStats(equipId, startTime, endTime);
    }

    @Override
    public boolean updateEquipStatus(Integer id, String status, Integer currentUserId, LocalDateTime borrowDate, LocalDateTime actualReturnDate) {
        return equipmentMapper.updateEquipStatus(id, status, currentUserId, borrowDate, actualReturnDate) > 0;
    }

    @Override
    public PageInfo<Equipment> selectPage(Equipment equipment, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Equipment> list = equipmentMapper.selectAll(
                equipment.getName(),
                equipment.getBrand(),
                equipment.getStatus(),
                equipment.getStorageId(),
                equipment.getCurrentUserId()
        );
        return PageInfo.of(list);
    }
}
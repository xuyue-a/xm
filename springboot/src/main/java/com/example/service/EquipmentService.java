package com.example.service;

import com.example.entity.Equipment;
import com.github.pagehelper.PageInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备服务接口
 */
public interface EquipmentService {

    /**
     * 条件查询设备
     */
    List<Equipment> selectAll(String name, String brand, String status, Integer storageId, Integer userId);

    /**
     * 新增设备
     */
    boolean addEquipment(Equipment equipment);

    /**
     * 修改设备
     */
    boolean updateEquipment(Equipment equipment);

    /**
     * 删除设备
     */
    boolean deleteEquipment(Integer id);

    /**
     * 根据ID查询设备
     */
    Equipment selectById(Integer id);

    /**
     * 设备使用统计
     */
    List<Map<String, Object>> selectEquipStats(Integer equipId, String startTime, String endTime);

    /**
     * 更新设备状态（审批后调用，修正参数为LocalDateTime）
     */
    boolean updateEquipStatus(Integer id, String status, Integer currentUserId, LocalDateTime borrowDate, LocalDateTime actualReturnDate);

    PageInfo<Equipment> selectPage(Equipment equipment, Integer pageNum, Integer pageSize);
}
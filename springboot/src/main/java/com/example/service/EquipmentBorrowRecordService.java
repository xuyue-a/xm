package com.example.service;

import com.example.entity.EquipmentBorrowRecord;
import com.github.pagehelper.PageInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备借用记录服务接口
 */
public interface EquipmentBorrowRecordService {

    /**
     * 条件查询借用/归还记录
     */
    List<EquipmentBorrowRecord> selectAll(Map<String, Object> params); // 核心修改


    /**
     * 新增借用/归还申请
     */
    boolean addRecord(EquipmentBorrowRecord record);

    /**
     * 修改记录
     */
    boolean updateRecord(EquipmentBorrowRecord record);

    /**
     * 审批记录
     */
    boolean reviewRecord(Integer id, String status, Integer reviewerId, LocalDateTime approveTime, String reviewRemark, String lendStatus);

    /**
     * 根据ID查询记录
     */
    EquipmentBorrowRecord selectById(Integer id);

    /**
     * 根据设备ID查询记录
     */
    List<EquipmentBorrowRecord> selectByEquipId(Integer equipId);

    /**
     * 根据ID删除记录
     */
    boolean deleteById(Integer id);

    PageInfo<EquipmentBorrowRecord> selectPage(Map<String, Object> params, Integer pageNum, Integer pageSize);
}
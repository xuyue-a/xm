package com.example.mapper;

import com.example.entity.EquipmentBorrowRecord;

import java.util.List;
import java.util.Map;

/**
 * 设备借用记录Mapper接口
 */
public interface EquipmentBorrowRecordMapper {

    /**
     * 条件查询借用/归还记录（新增 originalId 参数）
     */
    List<EquipmentBorrowRecord> selectAll(Map<String, Object> params);

    /**
     * 新增借用/归还申请
     */
    int insert(EquipmentBorrowRecord record);

    /**
     * 根据ID更新记录
     */
    int updateById(EquipmentBorrowRecord record);

    /**
     * 审批更新（仅更新审批相关字段）
     */
    int review(Map<String, Object> params);

    /**
     * 根据ID查询记录
     */
    EquipmentBorrowRecord selectById(Integer id);

    /**
     * 根据设备ID查询记录（统计用）
     */
    List<EquipmentBorrowRecord> selectByEquipId(Integer equipId);

    /**
     * 删除记录
     */
    int deleteById(Integer id);
}
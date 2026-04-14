package com.example.service.impl;

import com.example.entity.Equipment;
import com.example.entity.EquipmentBorrowRecord;
import com.example.mapper.EquipmentBorrowRecordMapper;
import com.example.service.EquipmentBorrowRecordService;
import com.example.service.EquipmentService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备借用记录服务实现类
 */
@Service
public class EquipmentBorrowRecordServiceImpl implements EquipmentBorrowRecordService {

    @Resource
    private EquipmentBorrowRecordMapper equipmentBorrowRecordMapper;
    @Resource
    private EquipmentService equipmentService;

    @Override
    public List<EquipmentBorrowRecord> selectAll(Map<String, Object> params) {
        // 直接传递 Map 参数给 Mapper
        return equipmentBorrowRecordMapper.selectAll(params);
    }

    @Override
    public boolean addRecord(EquipmentBorrowRecord record) {
        // 1. 归还申请：基于originalId精准校验重复（核心修复）
        if ("return".equals(record.getType())) {
            // 校验originalId非空
            if (record.getOriginalId() == null) {
                return false;
            }
            // 查询该originalId下是否有未完成的归还申请
            Map<String, Object> params = new HashMap<>();
            params.put("userId", record.getUserId());
            params.put("type", "return");
            params.put("originalId", record.getOriginalId());

            // 查询该originalId下是否有未完成的归还申请
            List<EquipmentBorrowRecord> existingReturns = selectAll(params);
            // 检查是否有待审批/已通过/已完成的归还申请
            boolean hasUnfinishedReturn = existingReturns.stream().anyMatch(item ->
                    "pending".equals(item.getStatus()) || "approved".equals(item.getStatus()) || "completed".equals(item.getStatus())
            );
            if (hasUnfinishedReturn) {
                return false;
            }
        }

        // 2. 插入申请记录
        int insert = equipmentBorrowRecordMapper.insert(record);
        if (insert > 0 && "borrow".equals(record.getType())) {
            // 3. 借用申请：更新设备状态为lock
            Equipment equipment = equipmentService.selectById(record.getEquipId());
            if (equipment != null) {
                equipmentService.updateEquipStatus(
                        equipment.getId(),
                        "lock",
                        record.getUserId(),
                        null,
                        null
                );
            }
        }
        return insert > 0;
    }

    @Override
    public boolean updateRecord(EquipmentBorrowRecord record) {
        return equipmentBorrowRecordMapper.updateById(record) > 0;
    }

    @Override
    public boolean reviewRecord(Integer id, String status, Integer reviewerId, LocalDateTime approveTime, String reviewRemark, String lendStatus) {
        // 1. 先查询记录，获取设备ID和类型
        EquipmentBorrowRecord record = equipmentBorrowRecordMapper.selectById(id);
        if (record == null) {
            return false;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("status", status);
        params.put("reviewerId", reviewerId);
        params.put("approveTime", approveTime);
        params.put("reviewRemark", reviewRemark);
        params.put("type", record.getType());
        params.put("lendStatus", lendStatus);

        // 3. 执行审批更新
        int update = equipmentBorrowRecordMapper.review(params);

        // 4. 同步更新设备状态
        if (update > 0) {
            Equipment equipment = equipmentService.selectById(record.getEquipId());
            if (equipment != null) {
                if ("approved".equals(status)) {
                    // 审批通过：保持原有逻辑不变
                    if ("borrow".equals(record.getType())) {
                        // 借用通过：设备状态改为borrowed，绑定当前用户
                        equipmentService.updateEquipStatus(
                                equipment.getId(),
                                "borrowed",
                                record.getUserId(),
                                LocalDateTime.now(),
                                null
                        );
                    } else if ("return".equals(record.getType())) {
                        // 归还通过：设备状态改为available，清空用户
                        equipmentService.updateEquipStatus(
                                equipment.getId(),
                                "available",
                                null,
                                null,
                                LocalDateTime.now()
                        );
                    }
                } else if ("rejected".equals(status)) {
                    // 核心新增：审批拒绝时，仅针对借用申请恢复设备状态为可用
                    if ("borrow".equals(record.getType())) {
                        equipmentService.updateEquipStatus(
                                equipment.getId(),
                                "available",  // 拒绝后设备变为可用
                                null,
                                null,
                                null
                        );
                    }
                    // 归还申请拒绝时：不修改设备状态，保持原有逻辑
                }
            }
        }
        return update > 0;
    }

    @Override
    public EquipmentBorrowRecord selectById(Integer id) {
        return equipmentBorrowRecordMapper.selectById(id);
    }

    @Override
    public List<EquipmentBorrowRecord> selectByEquipId(Integer equipId) {
        return equipmentBorrowRecordMapper.selectByEquipId(equipId);
    }

    @Override
    public boolean deleteById(Integer id) {
        // 1. 先查询记录
        EquipmentBorrowRecord record = equipmentBorrowRecordMapper.selectById(id);
        if (record == null || !"pending".equals(record.getStatus())) {
            return false;
        }

        // 2. 如果是借用申请，恢复设备状态
        if ("borrow".equals(record.getType())) {
            Equipment equipment = equipmentService.selectById(record.getEquipId());
            if (equipment != null && "lock".equals(equipment.getStatus())) {
                equipmentService.updateEquipStatus(
                        equipment.getId(),
                        "available",
                        null,
                        null,
                        null
                );
            }
        }

        // 3. 执行删除
        int deleteCount = equipmentBorrowRecordMapper.deleteById(id);
        return deleteCount > 0;
    }

    @Override
    public PageInfo<EquipmentBorrowRecord> selectPage(Map<String, Object> params, Integer pageNum, Integer pageSize) {
        // 开启分页
        PageHelper.startPage(pageNum, pageSize);
        // 复用原有条件查询逻辑
        List<EquipmentBorrowRecord> recordList = equipmentBorrowRecordMapper.selectAll(params);
        // 封装分页结果
        return PageInfo.of(recordList);
    }

}
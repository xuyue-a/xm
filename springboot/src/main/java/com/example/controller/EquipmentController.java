package com.example.controller;

import com.example.common.Result;
import com.example.entity.Equipment;
import com.example.service.EquipmentService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备管理控制器
 */
@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    @Resource
    private EquipmentService equipmentService;

    /**
     * 条件查询设备
     */
    @GetMapping("/list")
    public Result<List<Equipment>> list(@RequestParam(required = false) String name,
                                        @RequestParam(required = false) String brand,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) Integer storageId,
                                        @RequestParam(required = false) Integer userId) {
        List<Equipment> list = equipmentService.selectAll(name, brand, status, storageId, userId);
        return Result.success(list);
    }

    /**
     * 分页/条件查询设备
     */
    @GetMapping("/selectPage")
    public Result selectPage(Equipment equipment,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Equipment> pageInfo = equipmentService.selectPage(equipment, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 新增设备
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    public Result<Boolean> add(@RequestBody Equipment equipment) {
        // 默认状态为可用
        if (equipment.getStatus() == null || equipment.getStatus().isEmpty()) {
            equipment.setStatus("available");
        }
        boolean flag = equipmentService.addEquipment(equipment);
        return Result.success(flag);
    }

    /**
     * 修改设备
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping
    public Result<Boolean> update(@RequestBody Equipment equipment) {
        boolean flag = equipmentService.updateEquipment(equipment);
        return Result.success(flag);
    }

    /**
     * 删除设备
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        boolean flag = equipmentService.deleteEquipment(id);
        return Result.success(flag);
    }

    /**
     * 根据ID查询设备
     */
    @GetMapping("/{id}")
    public Result<Equipment> getById(@PathVariable Integer id) {
        Equipment equipment = equipmentService.selectById(id);
        return Result.success(equipment);
    }

    /**
     * 设备使用统计
     */
    @GetMapping("/stats/{equipId}")
    public Result<List<Map<String, Object>>> stats(@PathVariable Integer equipId,
                                                   @RequestParam(required = false) String startTime,
                                                   @RequestParam(required = false) String endTime) {
        List<Map<String, Object>> stats = equipmentService.selectEquipStats(equipId, startTime, endTime);
        return Result.success(stats);
    }

    /**
     * 更新设备状态（审批后调用）
     */
    @PutMapping("/status")
    public Result<Boolean> updateStatus(@RequestParam Integer id,
                                        @RequestParam String status,
                                        @RequestParam(required = false) Integer currentUserId,
                                        @RequestParam(required = false) String borrowDate,
                                        @RequestParam(required = false) String actualReturnDate) {
        LocalDateTime borrowDateTime = borrowDate == null ? null : LocalDateTime.parse(borrowDate);
        LocalDateTime returnDateTime = actualReturnDate == null ? null : LocalDateTime.parse(actualReturnDate);
        boolean flag = equipmentService.updateEquipStatus(id, status, currentUserId, borrowDateTime, returnDateTime);
        return Result.success(flag);
    }
}
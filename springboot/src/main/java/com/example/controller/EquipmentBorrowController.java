package com.example.controller;

import com.example.common.Result;
import com.example.dto.BorrowReviewDTO;
import com.example.entity.Equipment;
import com.example.entity.EquipmentBorrowRecord;
import com.example.service.EquipmentBorrowRecordService;
import com.example.service.EquipmentService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备借用/归还审批控制器
 */
@RestController
@RequestMapping("/api/borrow")
public class EquipmentBorrowController {

    @Resource
    private EquipmentBorrowRecordService equipmentBorrowRecordService;
    @Resource
    private EquipmentService equipmentService;

    @GetMapping("/page")
    public Result<PageInfo<EquipmentBorrowRecord>> selectPage(
            // 查询条件参数
            @RequestParam(required = false) Integer equipId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer reviewerId,
            @RequestParam(required = false) String equipName,
            @RequestParam(required = false) Integer originalId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            // 分页参数（默认第1页，每页10条）
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        // 封装查询条件
        Map<String, Object> params = new HashMap<>();
        params.put("equipId", equipId);
        params.put("userId", userId);
        params.put("status", status);
        params.put("type", type);
        params.put("reviewerId", reviewerId);
        params.put("equipName", equipName);
        params.put("originalId", originalId);
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        // 调用分页查询
        PageInfo<EquipmentBorrowRecord> pageInfo = equipmentBorrowRecordService.selectPage(params, pageNum, pageSize);
        return Result.success(pageInfo);
    }


    /**
     * 用户提交借用申请
     */
    @PostMapping("/apply/borrow")
    public Result<Boolean> applyBorrow(@RequestBody EquipmentBorrowRecord record) {
        record.setType("borrow");
        record.setStatus("pending");
        record.setApplyTime(LocalDateTime.now());
        // 同步预计归还时间到设备表
        Equipment equipment = equipmentService.selectById(record.getEquipId());
        if (equipment != null) {
            equipment.setExpectedReturnDate(record.getExpectedReturnDate());
            equipmentService.updateEquipment(equipment);
        }
        boolean flag = equipmentBorrowRecordService.addRecord(record);
        return Result.success(flag);
    }

    /**
     * 用户提交归还申请
     */
    @PostMapping("/apply/return")
    public Result<Boolean> applyReturn(@RequestBody EquipmentBorrowRecord record) {
        // 1. 校验originalId非空
        if (record.getOriginalId() == null) {
            return Result.error("缺少原借用记录ID，无法提交归还申请");
        }
        // 2. 查询原借用记录，获取其lendStatus
        EquipmentBorrowRecord originalBorrowRecord = equipmentBorrowRecordService.selectById(record.getOriginalId());
        if (originalBorrowRecord == null) {
            return Result.error("原借用记录不存在，无法提交归还申请");
        }
        // 3. 初始化归还申请信息
        record.setType("return");
        record.setStatus("pending");
        record.setApplyTime(LocalDateTime.now());
        record.setLendStatus(originalBorrowRecord.getLendStatus());
        // 4. 校验returnStatus非空
        if (record.getReturnStatus() == null || record.getReturnStatus().trim().isEmpty()) {
            return Result.error("归还状态不能为空，请填写设备归还状态");
        }
        // 5. 提交申请
        boolean flag = equipmentBorrowRecordService.addRecord(record);
        if (!flag) {
            return Result.error("该借用记录已提交归还申请，请勿重复提交");
        }
        return Result.success( "归还申请提交成功");
    }

    /**
     * 管理员审批（借用/归还）
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/review")
    public Result<Boolean> review(@RequestBody BorrowReviewDTO reviewDTO) {
        if (reviewDTO.getId() == null) {
            return Result.error("记录ID不能为空");
        }
        if (reviewDTO.getStatus() == null || !reviewDTO.getStatus().matches("approved|rejected")) {
            return Result.error("审批状态必须为approved或rejected");
        }
        if (reviewDTO.getReviewerId() == null) {
            return Result.error("审批人ID不能为空");
        }
        // 借用审批时，借出状态不能为空。借用审批拒绝时，无需校验借出状态
        EquipmentBorrowRecord record = equipmentBorrowRecordService.selectById(reviewDTO.getId());
        if (record == null) {
            return Result.error("记录不存在");
        }
        if ("borrow".equals(record.getType())
                && "approved".equals(reviewDTO.getStatus())  // 仅通过时校验
                && (reviewDTO.getLendStatus() == null || reviewDTO.getLendStatus().trim().isEmpty())) {
            return Result.error("借出状态不能为空");
        }

        LocalDateTime approveTime = LocalDateTime.now();
        boolean flag = equipmentBorrowRecordService.reviewRecord(
                reviewDTO.getId(),
                reviewDTO.getStatus(),
                reviewDTO.getReviewerId(),
                approveTime,
                reviewDTO.getReviewRemark(),
                reviewDTO.getLendStatus()
        );
        return Result.success(flag);
    }


    /**
     * 根据ID查询记录
     */
    @GetMapping("/{id}")
    public Result<EquipmentBorrowRecord> getById(@PathVariable Integer id) {
        EquipmentBorrowRecord record = equipmentBorrowRecordService.selectById(id);
        return Result.success(record);
    }

    /**
     * 根据设备ID查询记录
     */
    @GetMapping("/equip/{equipId}")
    public Result<List<EquipmentBorrowRecord>> getByEquipId(@PathVariable Integer equipId) {
        List<EquipmentBorrowRecord> list = equipmentBorrowRecordService.selectByEquipId(equipId);
        return Result.success(list);
    }

    /**
     * 修改申请记录（仅备注）
     */
    @PutMapping("/update")
    public Result<Boolean> updateRecord(@RequestBody EquipmentBorrowRecord record) {
        if (record.getId() == null) {
            return Result.error("记录ID不能为空");
        }
        EquipmentBorrowRecord oldRecord = equipmentBorrowRecordService.selectById(record.getId());
        if (oldRecord == null || !"pending".equals(oldRecord.getStatus())) {
            return Result.error("仅可修改待审批的记录");
        }
        oldRecord.setRemark(record.getRemark());
        boolean flag = equipmentBorrowRecordService.updateRecord(oldRecord);
        return Result.success(flag);
    }

    /**
     * 删除申请记录（仅待审批）
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRecord(@PathVariable Integer id) {
        boolean flag = equipmentBorrowRecordService.deleteById(id);
        if (!flag) {
            return Result.error("仅可删除待审批的记录");
        }
        return Result.success(flag);
    }
}
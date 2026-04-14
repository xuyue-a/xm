package com.example.mapper;

import com.example.entity.Equipment;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备Mapper接口
 */
public interface EquipmentMapper {

    /**
     * 分页/条件查询设备
     */
    List<Equipment> selectAll(@Param("name") String name,
                              @Param("brand") String brand,
                              @Param("status") String status,
                              @Param("storageId") Integer storageId,
                              @Param("userId") Integer userId);



    /**
     * 新增设备
     */
    int insert(Equipment equipment);

    /**
     * 根据ID更新设备
     */
    int updateById(Equipment equipment);

    /**
     * 根据ID删除设备
     */
    int deleteById(Integer id);

    /**
     * 根据ID查询设备
     */
    Equipment selectById(Integer id);

    /**
     * 设备使用统计：查询设备的借用归还记录
     */
    List<Map<String, Object>> selectEquipStats(@Param("equipId") Integer equipId,
                                               @Param("startTime") String startTime,
                                               @Param("endTime") String endTime);

    /**
     * 更新设备状态（修正参数为LocalDateTime）
     */
    int updateEquipStatus(@Param("id") Integer id,
                          @Param("status") String status,
                          @Param("currentUserId") Integer currentUserId,
                          @Param("borrowDate") LocalDateTime borrowDate,
                          @Param("actualReturnDate") LocalDateTime actualReturnDate);
}
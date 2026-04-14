package com.example.mapper;

import com.example.entity.ApplicationRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ApplicationRecordMapper {

    List<ApplicationRecord> selectAll(ApplicationRecord applicationRecord);

    @Select("select * from application_record where id = #{id}")
    ApplicationRecord selectById(Integer id);

    void insert(ApplicationRecord applicationRecord);

    void updateById(ApplicationRecord applicationRecord);

    @Delete("delete from `application_record` where id = #{id}")
    void deleteById(Integer id);

    void review(ApplicationRecord applicationRecord);

    // 新增：更新申请记录的item_id和item_type（分类ID）
    @Update("update application_record set item_id = #{itemId}, item_type = #{itemType} where id = #{id}")
    void updateItemAndTypeById(@Param("id") Integer id, @Param("itemId") Integer itemId, @Param("itemType") Integer itemType);

}


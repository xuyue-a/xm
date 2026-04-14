package com.example.mapper;

import com.example.entity.Academy;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AcademyMapper {

    List<Academy> selectAll(Academy academy);

    @Select("select * from academy where id = #{id}")
    Academy selectById(Integer id);

    void insert(Academy academy);

    void updateById(Academy academy);

    @Delete("delete from `academy` where id = #{id}")
    void deleteById(Integer id);

    // 新增：根据学院名称查询学院实体
    @Select("SELECT * FROM academy WHERE name = #{name}") // 注意表名和字段名需与数据库一致
    Academy selectByName(String name);

}

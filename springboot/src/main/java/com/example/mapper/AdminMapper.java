package com.example.mapper;

import com.example.entity.Admin;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface AdminMapper {

    List<Admin> selectAll(Admin admin);

    @Select("select * from `admin` where id = #{id}")
    Admin selectById(Integer id);

    void insert(Admin admin);

    void updateById(Admin admin);

    @Delete("delete from `admin` where id = #{id}")
    void deleteById(Integer id);

    @Select("select * from `admin` where username = #{username}")
    Admin selectByUsername(String username);

    @Update("update admin set password = #{password}, salt = #{salt} where id = #{id}")
    void updatePasswordAndSalt(Admin admin);

}

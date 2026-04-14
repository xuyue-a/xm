package com.example.mapper;

import com.example.entity.UserTable;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface UserTableMapper {

    List<UserTable> selectAll(UserTable userTable);

    @Select("select * from usertable where id = #{id}")
    UserTable selectById(Integer id);

    void insert(UserTable userTable);

    void updateById(UserTable userTable);

    @Delete("delete from `usertable` where id = #{id}")
    void deleteById(Integer id);

    @Select("select * from usertable where username = #{username}")
    UserTable selectByUsername(String username);

    @Update("update usertable set password = #{password}, salt = #{salt} where id = #{id}")
    void updatePasswordAndSalt(UserTable userTable);
}

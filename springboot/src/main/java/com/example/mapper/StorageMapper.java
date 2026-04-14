package com.example.mapper;

import com.example.entity.Storage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StorageMapper {

    List<Storage> selectAll(Storage storage);

    @Select("select * from storage where id = #{id}")
    Storage selectById(Integer id);

    void insert(Storage storage);

    void updateById(Storage storage);

    @Delete("delete from `storage` where id = #{id}")
    void deleteById(Integer id);

    @Select("select * from storage where name = #{name}")
    Storage selectByName(String name);
}

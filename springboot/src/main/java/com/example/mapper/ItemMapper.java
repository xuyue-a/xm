    package com.example.mapper;

    import com.example.entity.Item;
    import org.apache.ibatis.annotations.Delete;
    import org.apache.ibatis.annotations.Param;
    import org.apache.ibatis.annotations.Select;

    import java.util.List;

    public interface ItemMapper {

        List<Item> selectAll(Item item);

        @Select("select * from item where id = #{id}")
        Item selectById(Integer id);

        void insert(Item item);

        void updateById(Item item);

        @Delete("delete from `item` where id = #{id}")
        void deleteById(Integer id);

        @Select("select * from item where name = #{name}")
        List<Item> selectByName(String name);

        // 新增：根据分类ID查询物品
        List<Item> selectByTypeId(Integer typeId);

        // 新增：根据名称、分类ID、仓库ID查询唯一物品（精准查询新建物品）
        @Select("select * from item where name = #{name} and type_id = #{typeId} and storage_id = #{storageId}")
        Item selectByNameAndTypeIdAndStorageId(@Param("name") String name, @Param("typeId") Integer typeId, @Param("storageId") Integer storageId);

        // 核心新增：根据名称、分类ID、型号查询物品（判断自定义型号是否已存在）
        @Select("select * from item where name = #{name} and type_id = #{typeId} and model = #{model}")
        Item selectByNameAndTypeIdAndModel(@Param("name") String name, @Param("typeId") Integer typeId, @Param("model") String model);

        // 新增：查询分类下物品数量（用于生成物品代码）
        @Select("select count(1) from item where type_id = #{typeId}")
        int countByTypeId(Integer typeId);

        // 新增：批量删除
        void deleteBatch(List<Integer> ids);

        Item selectByNameAndModel(String name, String model);
    }
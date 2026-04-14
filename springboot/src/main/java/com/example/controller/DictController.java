package com.example.controller;

import com.example.common.Result;
import com.example.entity.Item;
import com.example.entity.ItemType;
import com.example.entity.Storage;
import com.example.service.ApplicationRecordService;
import com.example.service.ItemService;
import com.example.service.ItemTypeService;
import com.example.service.StorageService;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据字典控制器
 * 改造点：1. 新增物品名称去重接口 2. 调整型号查询为按物品名称查询 3. 补充按物品名称查库存接口
 */
@RestController
@RequestMapping("/dict")
public class DictController {

    @Resource
    private ItemTypeService itemTypeService;
    @Resource
    private ItemService itemService;
    @Resource
    private StorageService storageService;
    @Resource
    private ApplicationRecordService applicationRecordService;

    // ====================== 物品分类相关接口（不变） ======================
    @GetMapping("/itemType")
    public Result<List<ItemType>> getItemTypeList() {
        try {
            List<ItemType> itemTypeList = itemTypeService.selectAll();
            return Result.success(itemTypeList);
        } catch (Exception e) {
            return Result.error("查询物品分类失败：" + e.getMessage());
        }
    }

    @GetMapping("/itemType/name/{name}")
    public Result<ItemType> getItemTypeByName(@PathVariable String name) {
        if (!StringUtils.hasText(name)) {
            return Result.error("分类名称不能为空");
        }
        try {
            ItemType itemType = itemTypeService.selectByName(name);
            return Result.success(itemType);
        } catch (Exception e) {
            return Result.error("查询分类失败：" + e.getMessage());
        }
    }

    @PostMapping("/itemType/add")
    public Result<ItemType> addItemType(@RequestBody ItemType itemType) {
        if (!StringUtils.hasText(itemType.getName())) {
            return Result.error("分类名称不能为空");
        }
        if (!StringUtils.hasText(itemType.getTypeCode())) {
            return Result.error("分类代码不能为空");
        }
        try {
            boolean codeExist = itemTypeService.checkTypeCodeExist(itemType.getTypeCode());
            if (codeExist) {
                return Result.error("分类代码已存在，请更换");
            }
            ItemType existType = itemTypeService.selectByName(itemType.getName());
            if (existType != null) {
                return Result.success(existType);
            }
            itemTypeService.insert(itemType);
            ItemType newType = itemTypeService.selectByName(itemType.getName());
            return Result.success(newType);
        } catch (Exception e) {
            return Result.error("创建分类失败：" + e.getMessage());
        }
    }

    @GetMapping("/itemType/checkTypeCode/{typeCode}")
    public Result<Boolean> checkTypeCode(@PathVariable String typeCode) {
        if (!StringUtils.hasText(typeCode)) {
            return Result.success(true);
        }
        try {
            boolean exist = itemTypeService.checkTypeCodeExist(typeCode);
            return Result.success(exist);
        } catch (Exception e) {
            return Result.error("校验分类代码失败：" + e.getMessage());
        }
    }

    // ====================== 物品相关接口（核心改造） ======================
    /**
     * 原有接口：按分类ID查物品（含重复名称，保留供兼容）
     */
    @GetMapping("/item/{typeId}")
    public Result<List<Item>> getItemListByTypeId(@PathVariable Integer typeId) {
        if (typeId == null || typeId <= 0) {
            return Result.success(Collections.emptyList());
        }
        try {
            List<Item> itemList = itemService.selectByTypeId(typeId);
            return Result.success(itemList);
        } catch (Exception e) {
            return Result.error("查询物品列表失败：" + e.getMessage());
        }
    }

    /**
     * 新增接口1：按分类ID查询去重后的物品名称列表（解决下拉框重复问题）
     */
    @GetMapping("/item/uniqueName/{typeId}")
    public Result<List<String>> getUniqueItemNameByTypeId(@PathVariable Integer typeId) {
        if (typeId == null || typeId <= 0) {
            return Result.success(Collections.emptyList());
        }
        try {
            List<Item> itemList = itemService.selectByTypeId(typeId);
            // 去重：提取不重复的物品名称
            List<String> uniqueItemNames = itemList.stream()
                    .filter(item -> StringUtils.hasText(item.getName()))
                    .map(Item::getName)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            return Result.success(uniqueItemNames);
        } catch (Exception e) {
            return Result.error("查询去重物品名称失败：" + e.getMessage());
        }
    }

    /**
     * 新增接口2：按物品名称查询对应型号列表（实现“物品>型号”联动）
     */
    @GetMapping("/item/models/byName/{itemName}")
    public Result<List<String>> getModelsByItemName(@PathVariable String itemName) {
        if (!StringUtils.hasText(itemName)) {
            return Result.success(Collections.emptyList());
        }
        try {
            List<Item> itemList = itemService.selectByName(itemName);
            // 提取所有不重复的型号
            List<String> models = itemList.stream()
                    .filter(item -> StringUtils.hasText(item.getModel()))
                    .map(Item::getModel)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            return Result.success(models);
        } catch (Exception e) {
            return Result.error("查询物品型号失败：" + e.getMessage());
        }
    }

    /**
     * 新增接口3：按“物品名称+型号”查询物品ID（用于后续查询库存）
     */
    @GetMapping("/item/id/byNameAndModel")
    public Result<Item> getItemIdByNameAndModel(@RequestParam String itemName, @RequestParam String model) {
        if (!StringUtils.hasText(itemName) || !StringUtils.hasText(model)) {
            return Result.error("物品名称和型号不能为空");
        }
        try {
            List<Item> itemList = itemService.selectByName(itemName);
            // 匹配对应型号的物品
            Item targetItem = itemList.stream()
                    .filter(item -> model.equals(item.getModel()))
                    .findFirst()
                    .orElse(null);
            return Result.success(targetItem);
        } catch (Exception e) {
            return Result.error("查询物品ID失败：" + e.getMessage());
        }
    }

    /**
     * 原有接口：查询所有物品（不变）
     */
    @GetMapping("/item")
    public Result<List<Item>> getItemList() {
        try {
            List<Item> itemList = itemService.selectAll();
            return Result.success(itemList);
        } catch (Exception e) {
            return Result.error("查询所有物品失败：" + e.getMessage());
        }
    }

    /**
     * 原有接口：按物品ID查型号（保留供兼容）
     */
    @GetMapping("/item/models/{itemId}")
    public Result<List<String>> getModelsByItemId(@PathVariable Integer itemId) {
        if (itemId == null || itemId <= 0) {
            return Result.success(Collections.emptyList());
        }
        try {
            Item item = itemService.selectById(itemId);
            if (item == null || !StringUtils.hasText(item.getModel())) {
                return Result.success(Collections.emptyList());
            }
            String[] models = item.getModel().split(",");
            return Result.success(List.of(models));
        } catch (Exception e) {
            return Result.error("查询物品型号失败：" + e.getMessage());
        }
    }

    // ====================== 仓库相关接口（不变） ======================
    @GetMapping("/storage")
    public Result<List<Storage>> getStorageList() {
        try {
            List<Storage> storageList = storageService.selectAll();
            return Result.success(storageList);
        } catch (Exception e) {
            return Result.error("查询仓库列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/storage/byItemId/{itemId}")
    public Result<List<Storage>> getStorageByItemId(@PathVariable Integer itemId) {
        if (itemId == null || itemId <= 0) {
            return Result.success(Collections.emptyList());
        }
        try {
            Item item = itemService.selectById(itemId);
            if (item == null || item.getStorageId() == null) {
                return Result.success(Collections.emptyList());
            }
            Storage storage = storageService.selectById(item.getStorageId());
            if (storage == null) {
                return Result.success(Collections.emptyList());
            }
            return Result.success(Collections.singletonList(storage));
        } catch (Exception e) {
            return Result.error("查询物品对应仓库失败：" + e.getMessage());
        }
    }

    /**
     * 新增接口：按“物品名称+型号”查询对应仓库信息
     * 接口路径：/dict/item/storage/byNameAndModel
     */
    @GetMapping("/item/storage/byNameAndModel")
    public Result<List<Storage>> getStorageByItemNameAndModel(
            @RequestParam String itemName,
            @RequestParam String model) {
        // 1. 非空参数校验
        if (!StringUtils.hasText(itemName) || !StringUtils.hasText(model)) {
            return Result.success(Collections.emptyList());
        }
        try {
            // 2. 根据物品名称查询物品列表
            List<Item> itemList = itemService.selectByName(itemName);
            if (itemList == null || itemList.isEmpty()) {
                return Result.success(Collections.emptyList());
            }
            // 3. 筛选出匹配型号的目标物品
            Item targetItem = itemList.stream()
                    .filter(item -> model.equals(item.getModel()))
                    .findFirst()
                    .orElse(null);
            // 4. 若物品不存在，返回空列表
            if (targetItem == null || targetItem.getStorageId() == null) {
                return Result.success(Collections.emptyList());
            }
            // 5. 根据物品关联的storageId查询仓库信息
            Storage storage = storageService.selectById(targetItem.getStorageId());
            if (storage == null) {
                return Result.success(Collections.emptyList());
            }
            // 6. 返回仓库列表（保持和现有接口格式一致，用singletonList包装单个仓库）
            return Result.success(Collections.singletonList(storage));
        } catch (Exception e) {
            return Result.error("按物品名称+型号查询仓库失败：" + e.getMessage());
        }
    }
}
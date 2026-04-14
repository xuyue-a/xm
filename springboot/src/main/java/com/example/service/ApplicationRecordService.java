package com.example.service;

import com.example.entity.Account;
import com.example.entity.ApplicationRecord;
import com.example.entity.Item;
import com.example.entity.ItemType;
import com.example.mapper.ApplicationRecordMapper;
import com.example.service.impl.ItemServiceImpl;
import com.example.service.impl.ItemTypeServiceImpl;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationRecordService {

    @Resource
    private ApplicationRecordMapper applicationRecordMapper;
    @Resource
    private ItemService itemService;
    @Resource
    private ItemTypeService itemTypeService;
    @Resource
    private ItemTypeServiceImpl itemTypeServiceImpl;
    @Resource
    private ItemServiceImpl itemServiceImpl;


    private void ensureCorrectItemId(ApplicationRecord applicationRecord) {
        // 已有物品名称和型号，未传入itemId时，精准查询
        if (StringUtils.hasText(applicationRecord.getItemName())
                && StringUtils.hasText(applicationRecord.getModel())
                && applicationRecord.getItemId() == null) {
            Item targetItem = itemService.selectByNameAndModel(
                    applicationRecord.getItemName(),
                    applicationRecord.getModel()
            );
            if (targetItem != null) {
                applicationRecord.setItemId(targetItem.getId()); // 绑定正确的物品ID
            }
        }
    }

    public void add(ApplicationRecord applicationRecord) {
        applicationRecord.setApplyTime(LocalDateTime.now());
        applicationRecord.setStatus("pending");
        Account currenUser = TokenUtils.getCurrentUser();
        applicationRecord.setUserId(currenUser.getId());
        applicationRecord.setNameId(currenUser.getId());

        // 用途必填校验
        if (applicationRecord.getPurpose() == null || applicationRecord.getPurpose().isEmpty()) {
            throw new RuntimeException("用途不能为空");
        }
        // 领用场景
        if ("material_apply".equals(applicationRecord.getType())) {
            if (applicationRecord.getItemId() == null) {
                throw new RuntimeException("耗材领用必须选择物品（itemId不能为空）");
            }
            // 领用场景：有单价则计算总价
            if (applicationRecord.getUnitPrice() != null) {
                applicationRecord.setTotalPrice(applicationRecord.getUnitPrice().multiply(BigDecimal.valueOf(applicationRecord.getQuantity())));
            }

            // 同步物品名称和型号
            if (applicationRecord.getItemId() != null) {
                Item item = itemService.selectById(applicationRecord.getItemId());
                if (item != null) {
                    applicationRecord.setItemName(item.getName()); // 同步物品名称
                    applicationRecord.setModel(item.getModel()); // 同步物品型号
                }
            }
        }
        // 采购场景
        else if ("material_purchase".equals(applicationRecord.getType())) {
            // 请购部门必填
            if (applicationRecord.getPurchaseDept() == null || applicationRecord.getPurchaseDept().isEmpty()) {
                throw new RuntimeException("请购部门不能为空");
            }
            // 单价必填 + 计算总价
            if (applicationRecord.getUnitPrice() == null) {
                throw new RuntimeException("单价不能为空");
            }
            applicationRecord.setTotalPrice(applicationRecord.getUnitPrice().multiply(BigDecimal.valueOf(applicationRecord.getQuantity())));

            if (applicationRecord.getItemId() != null) {
                if (applicationRecord.getItemType() == null) {
                    throw new RuntimeException("选择已有物品时，物品分类不能为空");
                }
            } else {
                if (applicationRecord.getItemTypeStr() == null || applicationRecord.getItemTypeStr().isEmpty()) {
                    throw new RuntimeException("耗材采购必须填写物品分类名称");
                }
                if (applicationRecord.getItemNameTemp() == null || applicationRecord.getItemNameTemp().isEmpty()) {
                    throw new RuntimeException("耗材采购必须填写物品名称");
                }
                if (applicationRecord.getStorageId() == null) {
                    throw new RuntimeException("耗材采购必须选择仓库");
                }
            }
        }

        ensureCorrectItemId(applicationRecord);
        // 保存申请记录
        applicationRecordMapper.insert(applicationRecord);
    }

    /**
     * 审核申请记录（核心改造：自定义型号审核通过后创建新物品）
     */
    @Transactional(rollbackFor = Exception.class)
    public void review(ApplicationRecord applicationRecord) {
        Account currentAdmin = TokenUtils.getCurrentUser();
        applicationRecord.setReviewerId(currentAdmin.getId());
        applicationRecord.setReviewTime(LocalDateTime.now());

        // ========== 新增：自定义物品分类字段校验（关键修正） ==========
        ApplicationRecord record = applicationRecordMapper.selectById(applicationRecord.getId());
        if (record == null) {
            throw new RuntimeException("申请记录不存在");
        }

        // 自定义物品（itemId=null）：校验itemTypeStr是否有效
        if (record.getItemId() == null) {
            // 1. 去除分类名称前后空格，避免查询匹配失败
            String typeName = record.getItemTypeStr() == null ? "" : record.getItemTypeStr().trim();
            if (typeName.isEmpty()) {
                throw new RuntimeException("自定义物品审核失败：分类名称不能为空");
            }
            // 2. 提前校验分类是否存在（统一所有自定义物品场景）
            ItemType itemType = itemTypeService.selectByName(typeName);
            if (itemType == null) {
                // 若不是采购场景，不自动创建分类，直接提示分类不存在；若是采购场景，后续会自动创建
                if (!"material_purchase".equals(record.getType())) {
                    throw new RuntimeException("分类不存在|" + typeName);
                }
                // 采购场景无需在此抛出异常，后续handleMaterialPurchaseApproved会自动创建
            }
            // 3. 将处理后的分类名称回写，确保后续逻辑使用干净的名称
            record.setItemTypeStr(typeName);
        }

        applicationRecordMapper.review(applicationRecord);

        // 审核通过的逻辑
        if ("approved".equals(applicationRecord.getStatus())) {
            // 1. 领用场景：扣减物品库存（
            if ("material_apply".equals(record.getType())) {
                handleMaterialApplyApproved(record);
            }
            // 2. 采购场景：自动创建不存在的分类/物品 + 增加库存
            else if ("material_purchase".equals(record.getType())) {
                handleMaterialPurchaseApproved(record);
            }
        }
    }

    /**
     * 处理领用审核通过：扣减库存（库存不足则抛异常）
     */
    private void handleMaterialApplyApproved(ApplicationRecord record) {
        // 1. 优先校验 itemId（领用场景必须关联物品，itemId不能为空）
        if (record.getItemId() == null) {
            throw new RuntimeException("领用审核失败：物品ID不能为空");
        }

        // 2. 通过 itemId 查询准确的 Item 实体（获取可靠的物品名称和型号）
        Item item = itemService.selectById(record.getItemId());
        if (item == null) {
            throw new RuntimeException("领用审核失败：物品不存在，ID=" + record.getItemId());
        }

        // 3. 从 Item 实体中获取物品名称和型号
        String itemName = item.getName();
        String itemModel = item.getModel();
        // 校验物品名称和型号（从Item获取，更可靠）
        if (itemName == null || itemName.isEmpty() || itemModel == null || itemModel.isEmpty()) {
            throw new RuntimeException("领用审核失败：物品名称或型号不能为空（物品ID=" + record.getItemId() + "）");
        }

        // 4. 若需匹配申请记录中的型号（确保领用型号与物品型号一致）
        String applyModel = record.getModel();
        if (applyModel == null || !itemModel.contains(applyModel)) {
            throw new RuntimeException("领用审核失败：物品【" + itemName + "】不包含型号【" + applyModel + "】");
        }

        // 5. 校验库存并扣减
        Integer currentCount = item.getCount() == null ? 0 : item.getCount();
        if (currentCount < record.getQuantity()) {
            throw new RuntimeException("领用审核失败：物品【" + itemName + "-" + itemModel + "】库存不足，当前库存：" + currentCount + "，申请领用：" + record.getQuantity());
        }
        item.setCount(currentCount - record.getQuantity());
        itemService.update(item);
    }

    /**
     * 处理采购审核通过：自动创建不存在的分类/物品 + 增加库存（核心改造：使用modelTemp创建新物品型号）
     */
    private void handleMaterialPurchaseApproved(ApplicationRecord record) {
        // 优先获取留存的已有分类ID
        String typeName = record.getItemTypeStr() == null ? "" : record.getItemTypeStr().trim();
        String itemName = record.getItemNameTemp() == null ? "" : record.getItemNameTemp().trim();
        // 优先使用modelTemp（自定义型号），无则使用model
        String targetModel = record.getModelTemp() != null && !record.getModelTemp().trim().isEmpty()
                ? record.getModelTemp().trim()
                : (record.getModel() != null ? record.getModel().trim() : "");
        Integer storageId = record.getStorageId();
        Integer typeId = record.getItemType(); // 直接获取留存的已有分类ID，无需重新查询

        // 场景1：采购已有物品（itemId不为空）:判断是否有自定义型号，有则新建物品，无则更新库存
        if (record.getItemId() != null) {
            Item oldItem = itemService.selectById(record.getItemId());
            if (oldItem == null) {
                throw new RuntimeException("采购审核失败：物品不存在，ID=" + record.getItemId());
            }

            // 判断是否存在自定义型号（targetModel与原有物品型号不一致 → 新建物品）
            String oldItemModel = oldItem.getModel() == null ? "" : oldItem.getModel().trim();
            // 条件：有自定义型号 + 自定义型号与原有物品型号不一致 → 新建独立物品
            if (!targetModel.isEmpty() && !targetModel.equals(oldItemModel)) {
                // 1. 获取分类ID（沿用原有物品的分类ID）
                typeId = oldItem.getTypeId();
                itemName = oldItem.getName();
                storageId = record.getStorageId() != null ? record.getStorageId() : oldItem.getStorageId(); // 优先使用申请的存放位置

                // 2. 按【物品名称+分类ID+自定义型号】查询，确保不重复创建
                Item newItem = itemServiceImpl.selectByNameAndTypeIdAndModel(itemName, typeId, targetModel);
                if (newItem == null) {
                    // 新增物品：使用自定义型号、申请的存放位置
                    newItem = new Item();
                    newItem.setName(itemName);
                    newItem.setTypeId(typeId);
                    newItem.setStorageId(storageId);
                    newItem.setCount(record.getQuantity());
                    newItem.setModel(targetModel); // 存储自定义型号
                    newItem.setRemark("采购申请自动创建（已有物品自定义型号）：" + itemName + " - " + targetModel);
                    itemService.add(newItem); // 自动生成物品代码，新建独立物品
                } else {
                    // 已有的物品，仅累加库存
                    Integer currentCount = newItem.getCount() == null ? 0 : newItem.getCount();
                    newItem.setCount(currentCount + record.getQuantity());
                    itemService.update(newItem);
                }

                // 更新申请记录关联的新物品ID
                applicationRecordMapper.updateItemAndTypeById(record.getId(), newItem.getId(), typeId);
            } else {
                // 无自定义型号（使用原有型号）→ ，更新库存
                Integer currentCount = oldItem.getCount() == null ? 0 : oldItem.getCount();
                oldItem.setCount(currentCount + record.getQuantity());
                // 若有型号更新（非自定义，同原有型号），追加型号（可选）
                itemService.update(oldItem);
            }
        }
        // 场景2：采购自定义物品（itemId为空）—
        else {
            if ((typeId == null && typeName.isEmpty()) || itemName.isEmpty() || storageId == null) {
                throw new RuntimeException("采购审核失败：物品分类名称/ID、物品名称、仓库不能为空");
            }

            // 优先使用留存的typeId（已有分类），无需重新创建分类
            ItemType itemType = null;
            if (typeId != null) {
                // 已有分类ID：直接查询分类信息，无需创建
                itemType = itemTypeService.selectById(typeId);
                if (itemType == null) {
                    throw new RuntimeException("采购审核失败：所选分类不存在，ID=" + typeId);
                }
            } else {
                // 无分类ID（纯自定义分类）
                itemType = itemTypeService.selectByName(typeName);
                if (itemType == null) {
                    ItemType newItemType = new ItemType();
                    newItemType.setName(typeName);
                    newItemType.setRemark("入库新增：" + typeName);
                    itemTypeService.add(newItemType);
                    itemType = itemTypeService.selectByName(typeName);
                    if (itemType == null) {
                        throw new RuntimeException("采购审核失败：自动创建分类【" + typeName + "】失败");
                    }
                }
                typeId = itemType.getId();
            }

            // 按【物品名称+分类ID+型号】查询，确保自定义型号创建新物品
            Item item = itemServiceImpl.selectByNameAndTypeIdAndModel(itemName, typeId, targetModel);
            if (item == null) {
                // 新增物品：自动生成itemCode（依赖分类ID），使用自定义型号（targetModel）
                Item newItem = new Item();
                newItem.setName(itemName);
                newItem.setTypeId(typeId);
                newItem.setStorageId(storageId);
                newItem.setCount(record.getQuantity());
                newItem.setModel(targetModel); // 存储自定义型号
                newItem.setRemark("采购申请自动创建（自定义型号）：" + itemName + " - " + targetModel);
                itemService.add(newItem); // 自动生成itemCode
                item = itemServiceImpl.selectByNameAndTypeIdAndModel(itemName, typeId, targetModel);
                if (item == null) {
                    throw new RuntimeException("采购审核失败：自动创建物品【" + itemName + " - " + targetModel + "】失败");
                }
            } else {
                // 增加已有自定义物品的库存
                Integer currentCount = item.getCount() == null ? 0 : item.getCount();
                item.setCount(currentCount + record.getQuantity());
                if (!targetModel.isEmpty()) {
                    item.setModel(targetModel);
                }
                itemService.update(item);
            }

            applicationRecordMapper.updateItemAndTypeById(record.getId(), item.getId(), typeId);
        }
    }

    public void update(ApplicationRecord applicationRecord) {
        applicationRecord.setStatus("pending");
        applicationRecord.setApplyTime(LocalDateTime.now());
        // 编辑时重新计算总价
        if (applicationRecord.getUnitPrice() != null && applicationRecord.getQuantity() != null) {
            applicationRecord.setTotalPrice(applicationRecord.getUnitPrice().multiply(BigDecimal.valueOf(applicationRecord.getQuantity())));
        }

        // 同步物品名称和型号
        if ("material_apply".equals(applicationRecord.getType()) && applicationRecord.getItemId() != null) {
            Item item = itemService.selectById(applicationRecord.getItemId());
            if (item != null) {
                applicationRecord.setItemName(item.getName()); // 同步物品名称
                applicationRecord.setModel(item.getModel()); // 同步物品型号
            }
        }

        ensureCorrectItemId(applicationRecord);
        // 新增：更新申请记录
        applicationRecordMapper.updateById(applicationRecord);
    }

    public void deleteById(Integer id) {
        applicationRecordMapper.deleteById(id);
    }

    public List<ApplicationRecord> selectAll(ApplicationRecord applicationRecord) {
        return applicationRecordMapper.selectAll(applicationRecord);
    }

    public ApplicationRecord selectById(Integer id) {
        return applicationRecordMapper.selectById(id);
    }

    public PageInfo<ApplicationRecord> selectPage(ApplicationRecord applicationRecord, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        // 1. USER角色：仅查看自己的记录（原有逻辑保留，不改动）
        if ("USER".equals(currentUser.getRole())) {
            applicationRecord.setUserId(currentUser.getId());
        }
        // 2. SP_USER角色：强制过滤为采购类型（material_purchase）
        else if ("SP_USER".equals(currentUser.getRole())) {
            // 强制设置type为采购类型，覆盖前端传入的非采购类型（确保仅能查看采购记录）
            applicationRecord.setType("material_purchase");
            // 若SP_USER需要查看全量采购记录，无需设置userId；若需仅查看自己的采购，可追加：
            // applicationRecord.setUserId(currentUser.getId());
        }
        // ADMIN角色：无额外过滤，保留原有逻辑
        PageHelper.startPage(pageNum, pageSize);
        List<ApplicationRecord> list = applicationRecordMapper.selectAll(applicationRecord);
        return PageInfo.of(list);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            this.deleteById(id);
        }
    }

    public void updateItemType(Integer recordId, Integer itemType) {
        ApplicationRecord record = new ApplicationRecord();
        record.setId(recordId);
        record.setItemType(itemType);
        applicationRecordMapper.updateById(record);
    }

}
package com.example.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.example.common.Result;
import com.example.entity.ApplicationRecord;
import com.example.entity.Item;
import com.example.service.ApplicationRecordService;
import com.example.service.ItemService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.util.*;

/**
 * 申请记录控制器
 * 适配自定义型号(modelTemp)、审核逻辑优化、型号存储兼容
 * 核心改造：严格区分已有型号（model）和自定义型号（modelTemp）存储
 */
@RestController
@RequestMapping("/applicationRecord")
public class ApplicationRecordController {

    @Resource
    private ApplicationRecordService applicationRecordService;
    @Resource
    ItemService itemService;

    /**
     * 新增申请记录
     * 1.  关联物品（有itemId）：
     *    - 选择已有型号：存储到model，清空modelTemp
     *    - 自定义型号（物品无此型号）：model保留选中物品原有型号，自定义型号存入modelTemp
     * 2.  自定义物品（无itemId）：型号存入modelTemp（原有逻辑兼容）
     */
    @PostMapping("/add")
    public Result add(@RequestBody ApplicationRecord applicationRecord) {
        try {
            if (applicationRecord.getItemId() != null) {
                // a. 若前端传入model（已有型号），清空modelTemp
                if (applicationRecord.getModel() != null && !applicationRecord.getModel().isEmpty()) {
                    applicationRecord.setModelTemp(null);
                }
                // b. 若前端传入modelTemp（自定义型号），保留model（物品原有型号），不覆盖
            } else {
                // 自定义物品：优先存储modelTemp
                if (applicationRecord.getModelTemp() != null && !applicationRecord.getModelTemp().isEmpty()) {
                    // 自定义物品无原有型号，可将modelTemp同步到model
                    applicationRecord.setModel(applicationRecord.getModelTemp());
                }
            }
            applicationRecordService.add(applicationRecord);
            return Result.success("新增申请记录成功");
        } catch (Exception e) {
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 更新申请记录
     * 同新增逻辑，兼容model和modelTemp，严格区分已有/自定义型号
     */
    @PutMapping("/update")
    public Result update(@RequestBody ApplicationRecord applicationRecord) {
        try {
            // 型号字段适配
            if (applicationRecord.getItemId() != null) {
                // 关联已有物品：
                // a. 选择已有型号（model有值）：清空modelTemp
                if (applicationRecord.getModel() != null && !applicationRecord.getModel().isEmpty()) {
                    applicationRecord.setModelTemp(null);
                }
                // b. 自定义型号（modelTemp有值）：保留model字段，不覆盖
                if (applicationRecord.getModel() == null) {
                    applicationRecord.setModel(""); // 避免null值存储
                }
            } else {
                // 自定义物品：同步modelTemp到model（统一显示）
                if (applicationRecord.getModelTemp() != null && !applicationRecord.getModelTemp().isEmpty()) {
                    applicationRecord.setModel(applicationRecord.getModelTemp());
                }
            }
            applicationRecordService.update(applicationRecord);
            return Result.success("修改申请记录成功");
        } catch (Exception e) {
            return Result.error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 管理员审核申请记录
     * 优化点：
     * 1.  统一异常返回格式
     * 2.  分类不存在时返回分类名称，供前端创建
     * 3.  兼容自定义型号审核后创建新物品
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/review")
    public Result review(@RequestBody ApplicationRecord applicationRecord) {
        try {
            // 校验必要参数
            if (applicationRecord.getId() == null) {
                return Result.error("审核失败：申请记录ID不能为空");
            }
            if (applicationRecord.getStatus() == null || applicationRecord.getStatus().isEmpty()) {
                return Result.error("审核失败：审核状态不能为空");
            }
            applicationRecordService.review(applicationRecord);
            return Result.success("审核成功");
        } catch (RuntimeException e) {
            // 分类不存在异常：拆分消息返回分类名称
            if (e.getMessage() != null && e.getMessage().startsWith("分类不存在|")) {
                String typeName = e.getMessage().split("\\|")[1];
                return Result.error("分类不存在", typeName);
            }
            return Result.error("审核失败：" + e.getMessage());
        } catch (Exception e) {
            return Result.error("审核失败：系统异常，请稍后重试");
        }
    }

    // 新增：更新申请记录的分类ID
    @PutMapping("/updateType")
    public Result<?> updateType(@RequestBody ApplicationRecord record) {
        // 入参：record.id（申请记录ID）、record.itemType（新分类ID）
        applicationRecordService.updateItemType(record.getId(), record.getItemType());
        return Result.success();
    }

    /**
     * 删除单个申请记录
     * 增加参数校验
     */
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id) {
        try {
            if (id == null || id <= 0) {
                return Result.error("删除失败：无效的记录ID");
            }
            applicationRecordService.deleteById(id);
            return Result.success("删除申请记录成功");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除申请记录
     * 增加非空校验
     */
    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("删除失败：请选择要删除的记录");
            }
            applicationRecordService.deleteBatch(ids);
            return Result.success("批量删除申请记录成功");
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有申请记录（支持条件筛选）
     * 返回包含model和modelTemp的完整数据
     */
    @GetMapping("/selectAll")
    public Result selectAll(ApplicationRecord applicationRecord) {
        try {
            List<ApplicationRecord> list = applicationRecordService.selectAll(applicationRecord);
            // 适配前端显示：统一型号字段（优先model，无则modelTemp）
            list.forEach(record -> {
                if (record.getModel() == null && record.getModelTemp() != null) {
                    record.setModel(record.getModelTemp()); // 前端统一读取model
                }
            });
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询单个申请记录
     * 返回完整字段（含modelTemp）
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        try {
            if (id == null || id <= 0) {
                return Result.error("查询失败：无效的记录ID");
            }
            ApplicationRecord applicationRecord = applicationRecordService.selectById(id);
            // 适配前端显示
            if (applicationRecord != null && applicationRecord.getModel() == null && applicationRecord.getModelTemp() != null) {
                applicationRecord.setModel(applicationRecord.getModelTemp());
            }
            return Result.success(applicationRecord);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询申请记录
     * 优化点：
     * 1.  分页参数校验
     * 2.  适配型号字段显示
     */
    @GetMapping("/selectPage")
    public Result selectPage(ApplicationRecord applicationRecord,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // 分页参数校验
            if (pageNum < 1) pageNum = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;

            PageInfo<ApplicationRecord> pageInfo = applicationRecordService.selectPage(applicationRecord, pageNum, pageSize);

            pageInfo.getList().forEach(record -> {
                if (record.getModel() == null && record.getModelTemp() != null) {
                    record.setModel(record.getModelTemp());
                }
            });

            return Result.success(pageInfo);
        } catch (Exception e) {
            return Result.error("分页查询失败：" + e.getMessage());
        }
    }

    // 导出
    @GetMapping("/exportStock")
    public void exportStock(HttpServletResponse response) throws Exception {
        // 关闭缓冲区，确保数据实时输出
        response.setBufferSize(1024 * 1024);
        try {
            // 1. 修正查询条件：手动处理IN查询（替代错误的字符串拼接）
            List<ApplicationRecord> records = new ArrayList<>();
            // 分别查询approved和completed状态，合并结果
            ApplicationRecord query1 = new ApplicationRecord();
            query1.setStatus("approved");
            ApplicationRecord query2 = new ApplicationRecord();
            query2.setStatus("completed");
            records.addAll(applicationRecordService.selectAll(query1));
            records.addAll(applicationRecordService.selectAll(query2));

            // 打印日志：确认查询到的记录数（便于排查）
            System.out.println("查询到的申请记录数：" + records.size());
            if (records.isEmpty()) {
                response.setContentType("text/plain;charset=utf-8");
                response.getWriter().write("导出失败：无符合条件的申请记录");
                return;
            }

            // 2. 修正排序逻辑：增加异常处理，避免数字格式化报错
            records.sort((r1, r2) -> {
                String code1 = r1.getItemCode() == null ? "" : r1.getItemCode();
                String code2 = r2.getItemCode() == null ? "" : r2.getItemCode();

                // 拆分前缀（字母）和数字部分
                String prefix1 = code1.replaceAll("\\d+", "");
                String prefix2 = code2.replaceAll("\\d+", "");
                if (!prefix1.equals(prefix2)) {
                    return prefix1.compareTo(prefix2);
                }

                // 安全解析数字部分：避免空字符串报错
                String numStr1 = code1.replaceAll("\\D+", "");
                String numStr2 = code2.replaceAll("\\D+", "");
                // 若数字部分为空，默认按0处理
                Integer num1 = numStr1.isEmpty() ? 0 : Integer.parseInt(numStr1);
                Integer num2 = numStr2.isEmpty() ? 0 : Integer.parseInt(numStr2);
                return num1.compareTo(num2);
            });

            // 3. 统计每个item的入库/出库数量（按itemCode分组，逻辑不变）
            Map<String, Map<String, Integer>> itemStats = new HashMap<>();
            for (ApplicationRecord record : records) {
                String itemCode = record.getItemCode();
                if (itemCode == null) continue;

                // 初始化分组统计
                itemStats.computeIfAbsent(itemCode, k -> new HashMap<>() {{
                    put("purchase", 0); // 入库数量
                    put("apply", 0);    // 出库数量
                }});

                // 累加数量
                if ("material_purchase".equals(record.getType())) {
                    itemStats.get(itemCode).put("purchase",
                            itemStats.get(itemCode).get("purchase") + record.getQuantity());
                } else if ("material_apply".equals(record.getType())) {
                    itemStats.get(itemCode).put("apply",
                            itemStats.get(itemCode).get("apply") + record.getQuantity());
                }
            }

            // 4. 修正数据组装逻辑：宽松判断+去重，避免过滤有效数据
            List<Map<String, Object>> exportData = new ArrayList<>();
            // 用于去重：记录已处理的itemCode
            Set<String> processedItemCodes = new HashSet<>();

            for (ApplicationRecord record : records) {
                String itemCode = record.getItemCode();
                // 去重：同一itemCode只组装一次数据
                if (itemCode == null || processedItemCodes.contains(itemCode)) {
                    continue;
                }

                // 宽松处理：Item为null时，手动赋值默认值，不跳过数据
                Item item = null;
                if (record.getItemId() != null) {
                    item = itemService.selectById(record.getItemId());
                }

                // 获取统计数据
                Map<String, Integer> stats = itemStats.get(itemCode);
                int purchaseQty = stats == null ? 0 : stats.get("purchase");
                int applyQty = stats == null ? 0 : stats.get("apply");
                int balance = purchaseQty - applyQty;

                // 组装导出字段：Item为null时给默认值，避免字段缺失
                Map<String, Object> row = new HashMap<>();
                row.put("itemCode", itemCode);
                row.put("itemName", record.getItemName() == null ? "" : record.getItemName());
                row.put("name", item == null ? "" : item.getName()); // 耗材名称默认空
                row.put("balance", balance);     // 结余数量
                row.put("unit", item == null ? "" : item.getUnit()); // 规格单位默认空
                row.put("model", item == null ? (record.getModelTemp() != null ? record.getModelTemp() : "") : item.getModel()); // 兼容自定义型号
                row.put("manager", "");          // 管理人（空）
                row.put("count", item == null ? 0 : item.getCount()); // 清点数量默认0
                row.put("purchaseQty", purchaseQty); // 入库数量
                row.put("applyQty", applyQty);       // 出库数量

                exportData.add(row);
                // 标记已处理的itemCode，实现去重
                processedItemCodes.add(itemCode);
            }

            // 打印日志：确认组装的导出数据量
            System.out.println("组装的导出数据量：" + exportData.size());
            if (exportData.isEmpty()) {
                response.setContentType("text/plain;charset=utf-8");
                response.getWriter().write("导出失败：无有效数据可组装");
                return;
            }

            // 5. 生成Excel并导出（逻辑优化，确保写入成功）
            ExcelWriter writer = ExcelUtil.getWriter(true);
            // 设置表头别名
            writer.addHeaderAlias("itemCode", "代码");
            writer.addHeaderAlias("itemName", "类别");
            writer.addHeaderAlias("name", "耗材名称");
            writer.addHeaderAlias("balance", "结余数量");
            writer.addHeaderAlias("unit", "规格单位");
            writer.addHeaderAlias("model", "规格型号");
            writer.addHeaderAlias("manager", "管理人");
            writer.addHeaderAlias("count", "清点数量");
            writer.addHeaderAlias("purchaseQty", "入库数量");
            writer.addHeaderAlias("applyQty", "出库数量");

            // 写入数据（确保表头和数据对应）
            writer.write(exportData, true);

            // 6. 修正响应头配置：避免文件名乱码，确保Excel格式正确
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            String fileName = URLEncoder.encode("耗材库存表", "UTF-8");
            // 修正Content-Disposition格式，兼容更多浏览器
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + ".xlsx\"");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "no-cache");

            ServletOutputStream os = response.getOutputStream();
            writer.flush(os, true); // 强制刷新缓冲区
            os.flush(); // 刷新输出流，确保数据全部发送

            // 7. 资源关闭（规范释放）
            writer.close();
            os.close();
        } catch (Exception e) {
            // 异常时返回明确提示，便于排查
            response.setContentType("text/plain;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("导出失败：" + e.getMessage());
            // 打印异常堆栈，便于后端排查
            e.printStackTrace();
        }
    }

}
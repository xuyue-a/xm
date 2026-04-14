package com.example.entity;

public class Item {
    private Integer id;
    private String name;
    private String itemCode; // 物品代码（自动生成）
    private Integer storageId;
    private Integer typeId;
    private String storageName;
    private String typeName;
    private String unit;
    private Integer count;
    private String remark;
    private String model; // 物品型号


    // Getter & Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public Integer getStorageId() { return storageId; }
    public void setStorageId(Integer storageId) { this.storageId = storageId; }
    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer typeId) { this.typeId = typeId; }
    public String getStorageName() { return storageName; }
    public void setStorageName(String storageName) { this.storageName = storageName; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
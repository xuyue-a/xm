package com.example.entity;

public class ItemType {
    private int id;
    private String name;
    private String typeCode;
    private String remark;

    // Getter & Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
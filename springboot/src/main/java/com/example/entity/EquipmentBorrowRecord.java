package com.example.entity;

import java.time.LocalDateTime;

/**
 * 设备借用/归还记录（含审批）
 */
public class EquipmentBorrowRecord {
    private Integer id;
    private Integer equipId; // 设备ID
    private String equipName; // 设备名称（关联查询用）
    private String serialNumber; //设备序列号（资产编码）
    private Integer userId; // 申请人ID（关联UserTable）
    private String userName; // 申请人姓名（关联查询用）
    private Integer reviewerId; // 审批人ID（关联Admin）
    private String reviewerName; // 审批人姓名（关联查询用）
    private LocalDateTime applyTime; // 申请时间
    private LocalDateTime approveTime; // 审批时间
    private String status; // 状态：pending(待审批)/approved(已通过)/rejected(已拒绝)/completed(已完成)
    private String type; // 类型：borrow(借用)/return(归还)
    private String remark; // 申请备注
    private String reviewRemark; // 审批备注
    private LocalDateTime borrowTime; // 实际借用时间
    private LocalDateTime returnTime; // 实际归还时间
    private Integer borrowDays; // 借用天数
    private LocalDateTime expectedReturnDate; // 预计归还时间
    private Integer originalId; // 关联原借用记录ID（归还申请专用）
    private String lendStatus;
    private String returnStatus;

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEquipId() {
        return equipId;
    }

    public void setEquipId(Integer equipId) {
        this.equipId = equipId;
    }

    public String getEquipName() {
        return equipName;
    }

    public void setEquipName(String equipName) {
        this.equipName = equipName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Integer reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public LocalDateTime getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(LocalDateTime applyTime) {
        this.applyTime = applyTime;
    }

    public LocalDateTime getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(LocalDateTime approveTime) {
        this.approveTime = approveTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getReviewRemark() {
        return reviewRemark;
    }

    public void setReviewRemark(String reviewRemark) {
        this.reviewRemark = reviewRemark;
    }

    public LocalDateTime getBorrowTime() {
        return borrowTime;
    }

    public void setBorrowTime(LocalDateTime borrowTime) {
        this.borrowTime = borrowTime;
    }

    public LocalDateTime getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(LocalDateTime returnTime) {
        this.returnTime = returnTime;
    }

    public Integer getBorrowDays() {
        return borrowDays;
    }

    public void setBorrowDays(Integer borrowDays) {
        this.borrowDays = borrowDays;
    }

    public LocalDateTime getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(LocalDateTime expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public Integer getOriginalId() {
        return originalId;
    }

    public void setOriginalId(Integer originalId) {
        this.originalId = originalId;
    }

    public String getLendStatus() {
        return lendStatus;
    }

    public String getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(String returnStatus) {
        this.returnStatus = returnStatus;
    }

    public void setLendStatus(String lendStatus) {
        this.lendStatus = lendStatus;
    }
}
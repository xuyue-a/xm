package com.example.dto;

public class BorrowReviewDTO {
    private Integer id; // 对应前端的recordId
    private String status;
    private Integer reviewerId;
    private String reviewRemark;
    private String type;
    private String lendStatus;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Integer reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewRemark() {
        return reviewRemark;
    }

    public void setReviewRemark(String reviewRemark) {
        this.reviewRemark = reviewRemark;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLendStatus() {
        return lendStatus;
    }

    public void setLendStatus(String lendStatus) {
        this.lendStatus = lendStatus;
    }
}

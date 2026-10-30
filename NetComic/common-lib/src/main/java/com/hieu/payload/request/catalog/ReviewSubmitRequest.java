package com.hieu.payload.request.catalog;

import jakarta.validation.constraints.NotNull;

// Staff gửi chương đi duyệt. staffId là người bấm gửi, chưa lấy từ JWT.
public class ReviewSubmitRequest {

    @NotNull(message = "chapterId không được để trống")
    private Long chapterId;

    @NotNull(message = "staffId không được để trống")
    private Long staffId;

    public ReviewSubmitRequest() {
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }
}
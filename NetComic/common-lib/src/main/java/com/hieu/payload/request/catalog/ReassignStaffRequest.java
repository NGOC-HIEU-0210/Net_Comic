package com.hieu.payload.request.catalog;

import jakarta.validation.constraints.NotNull;

// Body của PUT: chuyển truyện từ nhân viên hiện tại (trên URL) sang nhân viên mới.
public class ReassignStaffRequest {

    @NotNull(message = "newStaffId không được để trống")
    private Long newStaffId;

    public ReassignStaffRequest() {
    }

    public Long getNewStaffId() {
        return newStaffId;
    }

    public void setNewStaffId(Long newStaffId) {
        this.newStaffId = newStaffId;
    }
}

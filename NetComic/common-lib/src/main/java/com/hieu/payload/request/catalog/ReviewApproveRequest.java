package com.hieu.payload.request.catalog;

import jakarta.validation.constraints.NotNull;

public class ReviewApproveRequest {

    @NotNull(message = "adminId không được để trống")
    private Long adminId;

    public ReviewApproveRequest() {
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }
}
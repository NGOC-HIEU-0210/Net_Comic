package com.hieu.payload.request.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LockAccountRequest {

    @NotBlank(message = "Lý do khóa tài khoản không được để trống")
    @Size(max = 500, message = "Lý do không được dài quá 500 ký tự")
    private String lockedReason;

    public LockAccountRequest() {
    }

    public LockAccountRequest(String lockedReason) {
        this.lockedReason = lockedReason;
    }

    public String getLockedReason() {
        return lockedReason;
    }

    public void setLockedReason(String lockedReason) {
        this.lockedReason = lockedReason;
    }
}

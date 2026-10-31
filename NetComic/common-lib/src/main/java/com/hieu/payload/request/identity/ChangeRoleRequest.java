package com.hieu.payload.request.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangeRoleRequest {

    @NotBlank(message = "Mã vai trò không được để trống")
    @Size(max = 30, message = "Mã vai trò không được vượt quá 30 ký tự")
    private String roleCode;

    public ChangeRoleRequest() {
    }

    public ChangeRoleRequest(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }
}

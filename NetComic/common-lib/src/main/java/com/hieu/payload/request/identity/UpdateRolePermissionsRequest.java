package com.hieu.payload.request.identity;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

// Body của PUT: thay toàn bộ permission của role.
public class UpdateRolePermissionsRequest {

    @NotEmpty(message = "Phải chọn ít nhất một permission")
    private Set<@NotNull(message = "ID permission không được để trống") Long> permissionIds = new HashSet<>();

    public UpdateRolePermissionsRequest() {
    }

    public UpdateRolePermissionsRequest(Set<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public Set<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(Set<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}

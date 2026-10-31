package com.hieu.identity_service.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hieu.identity_service.model.Permission;
import com.hieu.identity_service.model.Role;
import com.hieu.payload.request.identity.UpdateRolePermissionsRequest;
import com.hieu.payload.response.identity.PermissionResponse;
import com.hieu.payload.response.identity.RoleResponse;

public class RoleMapper {

    // Role -> RoleResponse (kèm danh sách permission dạng PermissionResponse)
    public static RoleResponse toResponse(Role role) {
        if (role == null) {
            return null;
        }
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setCode(role.getCode());
        response.setDescription(role.getDescription());

        List<PermissionResponse> permissions = new ArrayList<>();
        if (role.getPermissions() != null) {
            for (Permission permission : role.getPermissions()) {
                permissions.add(toPermissionResponse(permission));
            }
        }
        response.setPermissions(permissions);
        return response;
    }

    // Permission -> PermissionResponse
    public static PermissionResponse toPermissionResponse(Permission permission) {
        if (permission == null) {
            return null;
        }
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setCode(permission.getCode());
        response.setDescription(permission.getDescription());
        return response;
    }

    // permissionIds của UpdateRolePermissionsRequest -> Set<Permission> của role
    // (null/empty trong request nghĩa là bỏ toàn bộ permission)
    public static Set<Permission> toPermissionSet(UpdateRolePermissionsRequest request, List<Permission> permissions) {
        Set<Permission> result = new HashSet<>();
        if (request == null || request.getPermissionIds() == null || request.getPermissionIds().isEmpty()) {
            return result;
        }
        if (permissions == null) {
            return result;
        }
        for (Permission permission : permissions) {
            if (permission.getId() != null && request.getPermissionIds().contains(permission.getId())) {
                result.add(permission);
            }
        }
        return result;
    }
}

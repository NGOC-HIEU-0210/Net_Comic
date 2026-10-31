package com.hieu.identity_service.service;

import java.util.List;

import com.hieu.payload.request.identity.UpdateRolePermissionsRequest;
import com.hieu.payload.response.identity.PermissionResponse;
import com.hieu.payload.response.identity.RoleResponse;

public interface RoleService {

    // Role kèm danh sách permission
    List<RoleResponse> getAllRoles();

    // Thay toàn bộ permission của role; nên chặn sửa role ADMIN để không tự khóa quyền
    RoleResponse updateRolePermissions(Long roleId, UpdateRolePermissionsRequest request, Long adminId)
            throws Exception;

    List<PermissionResponse> getAllPermissions();
}

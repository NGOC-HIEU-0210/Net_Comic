package com.hieu.identity_service.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hieu.identity_service.mapper.RoleMapper;
import com.hieu.identity_service.model.Permission;
import com.hieu.identity_service.model.Role;
import com.hieu.identity_service.repository.PermissionRepository;
import com.hieu.identity_service.repository.RoleRepository;
import com.hieu.identity_service.service.RoleService;
import com.hieu.payload.request.identity.UpdateRolePermissionsRequest;
import com.hieu.payload.response.identity.PermissionResponse;
import com.hieu.payload.response.identity.RoleResponse;

import java.util.Set;

@Service
public class RoleServiceImpl implements RoleService {

    private static final String ADMIN_ROLE_CODE = "ADMIN";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    // Role kèm danh sách permission
    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RoleResponse> result = new ArrayList<>();
        for (Role role : roles) {
            result.add(RoleMapper.toResponse(role));
        }
        return result;
    }

    // Thay toàn bộ permission của role; nên chặn sửa role ADMIN để không tự khóa quyền
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleResponse updateRolePermissions(Long roleId, UpdateRolePermissionsRequest request, Long adminId) throws Exception {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new Exception("Không tìm thấy role với id = " + roleId));

        if (ADMIN_ROLE_CODE.equalsIgnoreCase(role.getCode())) {
            throw new Exception("Không thể sửa quyền của ADMIN");
        }

        Set<Permission> permissions = RoleMapper.toPermissionSet(request, permissionRepository.findAllById(request.getPermissionIds()));
        role.setPermissions(permissions);
        roleRepository.save(role);

        return RoleMapper.toResponse(role);
    }

    // Danh sách permission
    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        List<PermissionResponse> result = new ArrayList<>();
        for (Permission permission : permissions) {
            result.add(RoleMapper.toPermissionResponse(permission));
        }
        return result;
    }
}

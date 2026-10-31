package com.hieu.payload.response.identity;

import java.util.ArrayList;
import java.util.List;

public class RoleResponse {

    private Long id;
    private String code;
    private String description;

    // Role kèm danh sách permission, không lồng RoleResponse vào PermissionResponse
    private List<PermissionResponse> permissions = new ArrayList<>();

    public RoleResponse() {
    }

    public RoleResponse(Long id, String code, String description, List<PermissionResponse> permissions) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.permissions = permissions != null ? permissions : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PermissionResponse> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionResponse> permissions) {
        this.permissions = permissions;
    }
}

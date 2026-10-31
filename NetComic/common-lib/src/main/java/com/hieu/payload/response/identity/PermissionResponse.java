package com.hieu.payload.response.identity;

public class PermissionResponse {

    private Long id;

    // Ví dụ: author:read, author:create, comic:publish, chapter:import_zip
    private String code;
    private String description;

    public PermissionResponse() {
    }

    public PermissionResponse(Long id, String code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
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
}

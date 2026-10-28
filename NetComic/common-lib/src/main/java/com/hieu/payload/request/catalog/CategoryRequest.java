package com.hieu.payload.request.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryRequest {


    @NotBlank
    @Size(max = 85, message = "Tên không được dài quá 85 ký tự")
    private String name;


    private Boolean active = Boolean.TRUE;

    public CategoryRequest() {
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

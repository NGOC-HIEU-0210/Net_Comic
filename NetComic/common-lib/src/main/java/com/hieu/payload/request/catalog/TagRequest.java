package com.hieu.payload.request.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TagRequest {

    @NotBlank(message = "Tên tag không được để trống")
    @Size(max = 75, message = "Tên tag không được vượt quá 75 ký tự")
    private String name;

    private Boolean active = Boolean.TRUE;

    public TagRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}




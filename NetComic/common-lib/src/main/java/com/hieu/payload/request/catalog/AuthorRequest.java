package com.hieu.payload.request.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthorRequest {

    @NotBlank(message = "Tên tác giả không được để trống")
    @Size(max = 255, message = "Tên tác giả không được vượt quá 255 ký tự")
    private String name;

    private Boolean active = Boolean.TRUE;

    @NotBlank(message = "Tiểu sử không được để trống")
    @Size(max = 500, message = "Tiểu sử không được vượt quá 500 ký tự")
    private String bio;

    public AuthorRequest() {
    }

    public AuthorRequest(String name, Boolean active, String bio) {
        this.name = name;
        this.active = active != null ? active : Boolean.TRUE;
        this.bio = bio;
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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
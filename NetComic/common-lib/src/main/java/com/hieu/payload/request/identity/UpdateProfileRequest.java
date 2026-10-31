package com.hieu.payload.request.identity;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    // Null hoặc để trống thì giữ nguyên tên hiển thị hiện tại
    @Size(max = 100, message = "Tên hiển thị không được vượt quá 100 ký tự")
    private String displayName;

    // Null hoặc để trống thì giữ nguyên avatar hiện tại
    @Size(max = 512, message = "Đường dẫn ảnh không được vượt quá 512 ký tự")
    private String avatarUrl;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String displayName, String avatarUrl) {
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}

package com.hieu.payload.request.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RefreshTokenRequest {

    // Refresh token là opaque token (chỉ gửi qua client, DB lưu token hash dạng hex)
    @NotBlank(message = "Refresh token không được để trống")
    @Size(max = 512, message = "Refresh token không được vượt quá 512 ký tự")
    private String refreshToken;

    public RefreshTokenRequest() {
    }

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

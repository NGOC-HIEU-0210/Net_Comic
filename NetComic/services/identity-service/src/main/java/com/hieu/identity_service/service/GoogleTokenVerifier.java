package com.hieu.identity_service.service;

import com.hieu.payload.response.identity.GoogleUserInfo;

public interface GoogleTokenVerifier {

    // Kiểm tra chữ ký, hạn dùng và audience của ID token; trả sub, email, tên, ảnh
    GoogleUserInfo verify(String idToken) throws Exception;
}

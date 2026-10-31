package com.hieu.identity_service.service;

import com.hieu.payload.request.identity.*;
import com.hieu.payload.response.identity.AuthResponse;

public interface AuthService {

    // Tạo tài khoản role USER kèm hồ sơ, rồi đăng nhập luôn
    AuthResponse register(RegisterRequest request) throws Exception;

    // Kiểm tra mật khẩu, chặn tài khoản LOCKED (BR06), cập nhật lastLoginAt
    AuthResponse login(LoginRequest request) throws Exception;

    // Xác thực ID token Google; chưa có tài khoản thì tạo mới (USER + hồ sơ + OAuthIdentity)
    AuthResponse loginWithGoogle(GoogleLoginRequest request) throws Exception;

    // Đổi refresh token cũ lấy cặp token mới (token cũ bị thu hồi)
    AuthResponse refreshToken(RefreshTokenRequest request) throws Exception;

    void logout(RefreshTokenRequest request) throws Exception;

    // Luôn trả thành công dù email có tồn tại hay không, để không lộ email nào đã đăng ký
    void forgotPassword(ForgotPasswordRequest request) throws Exception;

    // Token hợp lệ, chưa dùng, chưa hết hạn; đổi xong thì thu hồi mọi refresh token
    void resetPassword(ResetPasswordRequest request) throws Exception;
}

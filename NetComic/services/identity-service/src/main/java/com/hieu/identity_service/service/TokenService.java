package com.hieu.identity_service.service;


import com.hieu.identity_service.model.Account;

// Quản lý JWT, RefreshToken và PasswordResetToken. Token gốc chỉ trả ra một lần, DB chỉ lưu bản băm.
public interface TokenService {

    String generateAccessToken(Account account);   // JWT chứa sub, role và danh sách permission

    long getAccessTokenExpirySeconds();

    String createRefreshToken(Account account);

    // Kiểm tra (tồn tại, chưa thu hồi, chưa hết hạn), thu hồi nó và trả tài khoản tương ứng
    Account useRefreshToken(String rawToken) throws Exception;

    void revokeRefreshToken(String rawToken);

    void revokeAllRefreshTokens(Long accountId);

    String createPasswordResetToken(Account account);

    // Đánh dấu usedAt và trả tài khoản; ném lỗi nếu token sai, đã dùng hoặc hết hạn
    Account consumePasswordResetToken(String rawToken) throws Exception;
}
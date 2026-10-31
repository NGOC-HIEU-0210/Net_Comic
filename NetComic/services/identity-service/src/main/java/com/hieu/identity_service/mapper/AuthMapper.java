package com.hieu.identity_service.mapper;

import com.hieu.identity_service.model.Account;
import com.hieu.identity_service.model.Role;
import com.hieu.identity_service.model.UserProfile;
import com.hieu.payload.request.identity.RegisterRequest;
import com.hieu.payload.response.identity.AuthResponse;

public class AuthMapper {

    public static final String TOKEN_TYPE_BEARER = "Bearer";

    // Tạo Account mới từ RegisterRequest. Role mặc định USER được nạp trước trong service.
    public static Account toAccount(RegisterRequest request, Role userRole, String passwordHash) {
        if (request == null || userRole == null) {
            return null;
        }
        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setPasswordHash(passwordHash);
        account.setRole(userRole);
        return account;
    }

    // Tạo UserProfile cho tài khoản (displayName null/blank thì lấy từ email)
    public static UserProfile toUserProfile(Account account, String displayName) {
        if (account == null) {
            return null;
        }
        UserProfile userProfile = new UserProfile();
        userProfile.setAccount(account);
        userProfile.setDisplayName(displayName != null && !displayName.isBlank() ? displayName : account.getEmail());
        return userProfile;
    }

    // Account + token -> AuthResponse (dùng cho register, login, refresh token)
    public static AuthResponse toAuthResponse(Account account, String accessToken, String refreshToken, Long expiresIn) {
        if (account == null) {
            return null;
        }
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccountId(account.getId());
        authResponse.setEmail(account.getEmail());
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setTokenType(TOKEN_TYPE_BEARER);
        authResponse.setExpiresIn(expiresIn);
        return authResponse;
    }
}

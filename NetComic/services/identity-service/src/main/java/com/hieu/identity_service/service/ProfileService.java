package com.hieu.identity_service.service;


import com.hieu.payload.request.identity.*;
import com.hieu.payload.response.identity.ProfileResponse;

public interface ProfileService {

    ProfileResponse getProfile(Long accountId) throws Exception;

    ProfileResponse updateProfile(Long accountId, UpdateProfileRequest request) throws Exception;

    // Kiểm tra mật khẩu cũ; đổi xong thì thu hồi các refresh token khác
    void changePassword(Long accountId, ChangePasswordRequest request) throws Exception;
}

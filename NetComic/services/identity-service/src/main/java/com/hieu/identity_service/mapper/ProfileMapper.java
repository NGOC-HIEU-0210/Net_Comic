package com.hieu.identity_service.mapper;

import com.hieu.identity_service.model.Account;
import com.hieu.identity_service.model.UserProfile;
import com.hieu.payload.request.identity.UpdateProfileRequest;
import com.hieu.payload.response.identity.ProfileResponse;

public class ProfileMapper {

    // Account + UserProfile -> ProfileResponse
    public static ProfileResponse toResponse(Account account, UserProfile userProfile) {
        if (account == null) {
            return null;
        }
        ProfileResponse response = new ProfileResponse();
        response.setAccountId(account.getId());
        response.setEmail(account.getEmail());
        response.setDisplayName(userProfile != null ? userProfile.getDisplayName() : null);
        response.setAvatarUrl(userProfile != null ? userProfile.getAvatarUrl() : null);
        response.setRoleCode(account.getRole() != null ? account.getRole().getCode() : null);
        response.setStatus(account.getStatus());
        return response;
    }

    // Cập nhật UserProfile từ UpdateProfileRequest (null = giữ nguyên)
    public static UserProfile toUserProfile(UserProfile userProfile, UpdateProfileRequest request) {
        if (userProfile == null || request == null) {
            return userProfile;
        }
        if (request.getDisplayName() != null) {
            userProfile.setDisplayName(request.getDisplayName());
        }
        if (request.getAvatarUrl() != null) {
            userProfile.setAvatarUrl(request.getAvatarUrl());
        }
        return userProfile;
    }
}

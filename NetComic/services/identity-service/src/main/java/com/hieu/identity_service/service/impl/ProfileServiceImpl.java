package com.hieu.identity_service.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hieu.identity_service.mapper.AuthMapper;
import com.hieu.identity_service.mapper.ProfileMapper;
import com.hieu.identity_service.model.Account;
import com.hieu.identity_service.model.UserProfile;
import com.hieu.identity_service.repository.AccountRepository;
import com.hieu.identity_service.repository.UserProfileRepository;
import com.hieu.identity_service.service.ProfileService;
import com.hieu.identity_service.service.TokenService;
import com.hieu.payload.request.identity.ChangePasswordRequest;
import com.hieu.payload.request.identity.UpdateProfileRequest;
import com.hieu.payload.response.identity.ProfileResponse;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public ProfileServiceImpl(AccountRepository accountRepository,
                              UserProfileRepository userProfileRepository,
                              TokenService tokenService,
                              PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.userProfileRepository = userProfileRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    // Lấy hồ sơ (profile) của tài khoản
    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long accountId) throws Exception {
        Account account = findAccountOrThrow(accountId);
        UserProfile userProfile = userProfileRepository.findByAccountId(accountId).orElse(null);
        return ProfileMapper.toResponse(account, userProfile);
    }

    // Cập nhật hồ sơ (profile)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProfileResponse updateProfile(Long accountId, UpdateProfileRequest request) throws Exception {
        Account account = findAccountOrThrow(accountId);

        UserProfile userProfile = userProfileRepository.findByAccountId(accountId)
                .orElseGet(() -> AuthMapper.toUserProfile(account, request != null ? request.getDisplayName() : null));
        UserProfile updated = ProfileMapper.toUserProfile(userProfile, request);
        userProfileRepository.save(updated);

        return ProfileMapper.toResponse(account, updated);
    }

    // Kiểm tra mật khẩu cũ; đổi xong thì thu hồi các refresh token khác
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long accountId, ChangePasswordRequest request) throws Exception {
        Account account = findAccountOrThrow(accountId);

        if (!passwordEncoder.matches(request.getOldPassword(), account.getPasswordHash())) {
            throw new Exception("Mật khẩu cũ không đúng");
        }

        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        tokenService.revokeAllRefreshTokens(accountId);
    }

    private Account findAccountOrThrow(Long accountId) throws Exception {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new Exception("Không tìm thấy tài khoản với id = " + accountId));
    }
}

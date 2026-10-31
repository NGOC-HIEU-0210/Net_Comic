package com.hieu.identity_service.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hieu.enums.AccountStatus;
import com.hieu.identity_service.mapper.AuthMapper;
import com.hieu.identity_service.model.Account;
import com.hieu.identity_service.model.OAuthIdentity;
import com.hieu.identity_service.model.Role;
import com.hieu.identity_service.model.UserProfile;
import com.hieu.identity_service.repository.AccountRepository;
import com.hieu.identity_service.repository.OAuthIdentityRepository;
import com.hieu.identity_service.repository.RoleRepository;
import com.hieu.identity_service.repository.UserProfileRepository;
import com.hieu.identity_service.service.AuthService;
import com.hieu.identity_service.service.EmailService;
import com.hieu.identity_service.service.GoogleTokenVerifier;
import com.hieu.identity_service.service.TokenService;
import com.hieu.payload.request.identity.ForgotPasswordRequest;
import com.hieu.payload.request.identity.GoogleLoginRequest;
import com.hieu.payload.request.identity.LoginRequest;
import com.hieu.payload.request.identity.RegisterRequest;
import com.hieu.payload.request.identity.RefreshTokenRequest;
import com.hieu.payload.request.identity.ResetPasswordRequest;
import com.hieu.payload.response.identity.AuthResponse;
import com.hieu.payload.response.identity.GoogleUserInfo;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_USER_ROLE_CODE = "USER";

    private final TokenService tokenService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final EmailService emailService;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final UserProfileRepository userProfileRepository;
    private final OAuthIdentityRepository oauthIdentityRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(TokenService tokenService,
                           GoogleTokenVerifier googleTokenVerifier,
                           EmailService emailService,
                           AccountRepository accountRepository,
                           RoleRepository roleRepository,
                           UserProfileRepository userProfileRepository,
                           OAuthIdentityRepository oauthIdentityRepository,
                           PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.googleTokenVerifier = googleTokenVerifier;
        this.emailService = emailService;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.userProfileRepository = userProfileRepository;
        this.oauthIdentityRepository = oauthIdentityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Tạo tài khoản role USER kèm hồ sơ, rồi đăng nhập luôn
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse register(RegisterRequest request) throws Exception {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new Exception("Email đã được đăng ký");
        }

        Role userRole = getUserRole();

        Account account = AuthMapper.toAccount(request, userRole, passwordEncoder.encode(request.getPassword()));
        accountRepository.save(account);

        UserProfile userProfile = AuthMapper.toUserProfile(account, request.getDisplayName());
        userProfileRepository.save(userProfile);

        String accessToken = tokenService.generateAccessToken(account);
        String refreshToken = tokenService.createRefreshToken(account);
        return AuthMapper.toAuthResponse(account, accessToken, refreshToken, tokenService.getAccessTokenExpirySeconds());
    }

    // Kiểm tra mật khẩu, chặn tài khoản LOCKED (BR06), cập nhật lastLoginAt
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse login(LoginRequest request) throws Exception {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new Exception("Email hoặc mật khẩu không đúng"));

        if (account.getStatus() == AccountStatus.LOCKED) {
            throw new Exception("Tài khoản đang bị khóa");
        }

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new Exception("Email hoặc mật khẩu không đúng");
        }

        account.setLastLoginAt(LocalDateTime.now());
        accountRepository.save(account);

        String accessToken = tokenService.generateAccessToken(account);
        String refreshToken = tokenService.createRefreshToken(account);
        return AuthMapper.toAuthResponse(account, accessToken, refreshToken, tokenService.getAccessTokenExpirySeconds());
    }

    // Xác thực ID token Google; chưa có tài khoản thì tạo mới (USER + hồ sơ + OAuthIdentity)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) throws Exception {
        GoogleUserInfo googleUserInfo = googleTokenVerifier.verify(request.getIdToken());

        Account account = accountRepository.findByEmail(googleUserInfo.getEmail()).orElseGet(() -> createGoogleAccount(googleUserInfo));
        if (account.getStatus() == AccountStatus.LOCKED) {
            throw new Exception("Tài khoản đang bị khóa");
        }

        String accessToken = tokenService.generateAccessToken(account);
        String refreshToken = tokenService.createRefreshToken(account);
        return AuthMapper.toAuthResponse(account, accessToken, refreshToken, tokenService.getAccessTokenExpirySeconds());
    }

    // Đổi refresh token cũ lấy cặp token mới (token cũ bị thu hồi)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse refreshToken(RefreshTokenRequest request) throws Exception {
        Account account = tokenService.useRefreshToken(request.getRefreshToken());

        String accessToken = tokenService.generateAccessToken(account);
        String refreshToken = tokenService.createRefreshToken(account);
        return AuthMapper.toAuthResponse(account, accessToken, refreshToken, tokenService.getAccessTokenExpirySeconds());
    }

    @Override
    public void logout(RefreshTokenRequest request) throws Exception {
        tokenService.revokeRefreshToken(request.getRefreshToken());
    }

    // Luôn trả thành công dù email có tồn tại hay không, để không lộ email nào đã đăng ký
    @Override
    public void forgotPassword(ForgotPasswordRequest request) throws Exception {
        accountRepository.findByEmail(request.getEmail()).ifPresent(account -> {
            String rawToken = tokenService.createPasswordResetToken(account);
            emailService.sendPasswordResetEmail(account.getEmail(), rawToken);
        });
    }

    // Token hợp lệ, chưa dùng, chưa hết hạn; đổi xong thì thu hồi mọi refresh token
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequest request) throws Exception {
        Account account = tokenService.consumePasswordResetToken(request.getToken());

        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }

    private Role getUserRole() {
        return roleRepository.findByCode(DEFAULT_USER_ROLE_CODE)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role USER"));
    }

    private Account createGoogleAccount(GoogleUserInfo googleUserInfo) {
        Account account = new Account();
        account.setEmail(googleUserInfo.getEmail());
        account.setPasswordHash(null);
        account.setStatus(AccountStatus.ACTIVE);
        account.setRole(getUserRole());

        OAuthIdentity oauthIdentity = new OAuthIdentity();
        oauthIdentity.setAccount(account);
        oauthIdentity.setProvider(com.hieu.enums.AuthProvider.GOOGLE);
        oauthIdentity.setProviderUserId(googleUserInfo.getSub());
        oauthIdentityRepository.save(oauthIdentity);

        return accountRepository.save(account);
    }
}

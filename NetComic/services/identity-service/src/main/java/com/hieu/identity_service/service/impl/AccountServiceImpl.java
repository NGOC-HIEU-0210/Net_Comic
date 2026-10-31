package com.hieu.identity_service.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hieu.enums.AccountStatus;
import com.hieu.identity_service.mapper.AccountMapper;
import com.hieu.identity_service.mapper.AuthMapper;
import com.hieu.identity_service.model.Account;
import com.hieu.identity_service.model.Role;
import com.hieu.identity_service.model.UserProfile;
import com.hieu.identity_service.repository.AccountRepository;
import com.hieu.identity_service.repository.RoleRepository;
import com.hieu.identity_service.repository.UserProfileRepository;
import com.hieu.identity_service.service.AccountService;
import com.hieu.identity_service.service.TokenService;
import com.hieu.payload.request.identity.ChangeRoleRequest;
import com.hieu.payload.request.identity.CreateStaffRequest;
import com.hieu.payload.request.identity.LockAccountRequest;
import com.hieu.payload.response.identity.AccountResponse;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final TokenService tokenService;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountServiceImpl(AccountRepository accountRepository,
                              RoleRepository roleRepository,
                              TokenService tokenService,
                              UserProfileRepository userProfileRepository,
                              PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.tokenService = tokenService;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Tìm theo email/tên; roleCode và status là bộ lọc tùy chọn (null = bỏ qua)
    @Override
    public Page<AccountResponse> searchAccounts(String keyword, String roleCode, AccountStatus status, Pageable pageable) {
        String kw = keyword == null ? null : keyword.trim();

        Page<Account> page;
        if (kw == null) {
            page = accountRepository.findAll(pageable);
        } else if (roleCode == null && status == null) {
            page = accountRepository.findByEmailContainingIgnoreCase(kw, pageable);
        } else if (status == null) {
            page = accountRepository.findByEmailContainingIgnoreCaseAndRole_Code(kw, roleCode, pageable);
        } else if (roleCode == null) {
            page = accountRepository.findByEmailContainingIgnoreCaseAndStatus(kw, status, pageable);
        } else {
            page = accountRepository.findByEmailContainingIgnoreCaseAndRole_CodeAndStatus(kw, roleCode, status, pageable);
        }

        return page.map(AccountMapper::toResponse);
    }

    @Override
    public AccountResponse getAccountById(Long id) throws Exception {
        return AccountMapper.toResponse(findAccountOrThrow(id));
    }

    // adminId lấy từ JWT, ghi vào createdBy
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountResponse createStaff(CreateStaffRequest request, Long adminId) throws Exception {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new Exception("Email đã được đăng ký");
        }

        Role role = roleRepository.findByCode(request.getRoleCode())
                .orElseThrow(() -> new Exception("Không tìm thấy role với code = " + request.getRoleCode()));

        Account account = AccountMapper.toAccount(request, role, passwordEncoder.encode(request.getPassword()), adminId);
        account = accountRepository.save(account);

        if (request.getDisplayName() != null && !request.getDisplayName().isBlank()) {
            UserProfile userProfile = AuthMapper.toUserProfile(account, request.getDisplayName());
            userProfileRepository.save(userProfile);
        }

        return AccountMapper.toResponse(account);
    }

    // Không cho Admin tự hạ quyền chính mình
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountResponse changeRole(Long id, ChangeRoleRequest request, Long adminId) throws Exception {
        Account account = findAccountOrThrow(id);

        if (id.equals(adminId)) {
            throw new Exception("Admin không thể tự hạ quyền của chính mình");
        }

        Role newRole = roleRepository.findByCode(request.getRoleCode())
                .orElseThrow(() -> new Exception("Không tìm thấy role với code = " + request.getRoleCode()));

        account.setRole(newRole);
        account = accountRepository.save(account);

        return AccountMapper.toResponse(account);
    }

    // Ghi lockedBy, lockedReason, lockedAt; thu hồi mọi refresh token; không khóa chính mình
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountResponse lockAccount(Long id, LockAccountRequest request, Long adminId) throws Exception {
        Account account = findAccountOrThrow(id);

        if (id.equals(adminId)) {
            throw new Exception("Admin không thể khóa chính mình");
        }

        account.setStatus(AccountStatus.LOCKED);
        account.setLockedBy(adminId);
        account.setLockedReason(request.getLockedReason());
        account.setLockedAt(LocalDateTime.now());
        account = accountRepository.save(account);

        tokenService.revokeAllRefreshTokens(id);

        return AccountMapper.toResponse(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountResponse unlockAccount(Long id, Long adminId) throws Exception {
        Account account = findAccountOrThrow(id);

        account.setStatus(AccountStatus.ACTIVE);
        account.setLockedBy(null);
        account.setLockedReason(null);
        account.setLockedAt(null);
        account = accountRepository.save(account);

        return AccountMapper.toResponse(account);
    }

    private Account findAccountOrThrow(Long id) throws Exception {
        return accountRepository.findById(id)
                .orElseThrow(() -> new Exception("Không tìm thấy tài khoản với id = " + id));
    }
}

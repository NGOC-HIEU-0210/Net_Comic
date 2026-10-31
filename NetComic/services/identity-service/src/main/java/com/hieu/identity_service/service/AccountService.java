package com.hieu.identity_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hieu.enums.AccountStatus;
import com.hieu.payload.request.identity.*;
import com.hieu.payload.response.identity.AccountResponse;

public interface AccountService {

    // Tìm theo email/tên; roleCode và status là bộ lọc tùy chọn (null = bỏ qua)
    Page<AccountResponse> searchAccounts(String keyword, String roleCode, AccountStatus status, Pageable pageable);

    AccountResponse getAccountById(Long id) throws Exception;

    // adminId lấy từ JWT, ghi vào createdBy
    AccountResponse createStaff(CreateStaffRequest request, Long adminId) throws Exception;

    // Không cho Admin tự hạ quyền chính mình
    AccountResponse changeRole(Long id, ChangeRoleRequest request, Long adminId) throws Exception;

    // Ghi lockedBy, lockedReason, lockedAt; thu hồi mọi refresh token; không khóa chính mình
    AccountResponse lockAccount(Long id, LockAccountRequest request, Long adminId) throws Exception;

    AccountResponse unlockAccount(Long id, Long adminId) throws Exception;
}
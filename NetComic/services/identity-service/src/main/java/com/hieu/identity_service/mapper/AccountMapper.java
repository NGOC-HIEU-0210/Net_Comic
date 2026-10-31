package com.hieu.identity_service.mapper;

import com.hieu.enums.AccountStatus;
import com.hieu.identity_service.model.Account;
import com.hieu.identity_service.model.Role;
import com.hieu.payload.request.identity.CreateStaffRequest;
import com.hieu.payload.response.identity.AccountResponse;

public class AccountMapper {

    // Account -> AccountResponse (dùng cho getAccountById / createStaff / changeRole / lock / unlock)
    public static AccountResponse toResponse(Account account) {
        if (account == null) {
            return null;
        }
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setEmail(account.getEmail());
        response.setStatus(account.getStatus());
        response.setRoleCode(account.getRole() != null ? account.getRole().getCode() : null);
        response.setLockedReason(account.getLockedReason());
        response.setLockedBy(account.getLockedBy());
        response.setLockedAt(account.getLockedAt());
        response.setLastLoginAt(account.getLastLoginAt());
        response.setCreatedBy(account.getCreatedBy());
        response.setCreatedAt(account.getCreatedAt());
        return response;
    }

    // Tạo Account cho staff mới (status ACTIVE, createdBy là adminId từ JWT)
    public static Account toAccount(CreateStaffRequest request, Role role, String passwordHash, Long createdBy) {
        if (request == null || role == null) {
            return null;
        }
        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setPasswordHash(passwordHash);
        account.setRole(role);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedBy(createdBy);
        return account;
    }
}

package com.hieu.payload.response.identity;

import com.hieu.enums.AccountStatus;

public class ProfileResponse {

    private Long accountId;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String roleCode;
    private AccountStatus status;

    public ProfileResponse() {
    }

    public ProfileResponse(Long accountId, String email, String displayName, String avatarUrl, String roleCode, AccountStatus status) {
        this.accountId = accountId;
        this.email = email;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.roleCode = roleCode;
        this.status = status;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}

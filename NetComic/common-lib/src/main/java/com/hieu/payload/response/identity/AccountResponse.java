package com.hieu.payload.response.identity;

import java.time.LocalDateTime;

import com.hieu.enums.AccountStatus;

public class AccountResponse {

    private Long id;
    private String email;

    private AccountStatus status;
    private String roleCode;

    private String lockedReason;
    private Long lockedBy;
    private LocalDateTime lockedAt;

    private LocalDateTime lastLoginAt;
    private Long createdBy;
    private LocalDateTime createdAt;

    public AccountResponse() {
    }

    public AccountResponse(Long id, String email, AccountStatus status, String roleCode, String lockedReason, Long lockedBy, LocalDateTime lockedAt, LocalDateTime lastLoginAt, Long createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.status = status;
        this.roleCode = roleCode;
        this.lockedReason = lockedReason;
        this.lockedBy = lockedBy;
        this.lockedAt = lockedAt;
        this.lastLoginAt = lastLoginAt;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getLockedReason() {
        return lockedReason;
    }

    public void setLockedReason(String lockedReason) {
        this.lockedReason = lockedReason;
    }

    public Long getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(Long lockedBy) {
        this.lockedBy = lockedBy;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

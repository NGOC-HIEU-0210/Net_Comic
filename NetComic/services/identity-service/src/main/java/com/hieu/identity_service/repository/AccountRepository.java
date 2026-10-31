package com.hieu.identity_service.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hieu.enums.AccountStatus;
import com.hieu.identity_service.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Account> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    Page<Account> findByEmailContainingIgnoreCaseAndRole_Code(String email, String roleCode, Pageable pageable);

    Page<Account> findByEmailContainingIgnoreCaseAndStatus(String email, AccountStatus status, Pageable pageable);

    Page<Account> findByEmailContainingIgnoreCaseAndRole_CodeAndStatus(String email, String roleCode, AccountStatus status, Pageable pageable);
}

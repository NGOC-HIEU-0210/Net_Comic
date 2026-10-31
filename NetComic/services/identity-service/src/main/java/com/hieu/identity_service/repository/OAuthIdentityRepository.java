package com.hieu.identity_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hieu.enums.AuthProvider;
import com.hieu.identity_service.model.OAuthIdentity;

@Repository
public interface OAuthIdentityRepository extends JpaRepository<OAuthIdentity, Long> {

    Optional<OAuthIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}

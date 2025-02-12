package com.doldev.dollog.domain.account.snsUser.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.snsUser.enums.SnsProvider;

public interface SnsUserRepository extends JpaRepository<SnsUser, Integer> {
    Optional<SnsUser> findBySnsIdentifierAndProvider(String snsIdentifier, SnsProvider provider);
}

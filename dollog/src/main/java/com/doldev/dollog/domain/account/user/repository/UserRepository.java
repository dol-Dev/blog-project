package com.doldev.dollog.domain.account.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.doldev.dollog.domain.account.user.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}

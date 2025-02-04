package com.doldev.dollog.repository.user;

import java.util.Optional;
import com.doldev.dollog.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

}

package com.doldev.dollog.domain.user.application;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doldev.dollog.domain.roletype.entity.RoleType;
import com.doldev.dollog.domain.user.dto.req.SignupReqDto;
import com.doldev.dollog.domain.user.entity.User;
import com.doldev.dollog.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final BCryptPasswordEncoder encoder;
    private final UserRepository userRepository;

    // 회원 가입
    @Transactional
    public void signup(SignupReqDto req) {

        // 사용자 생성
        User signupUser = User.builder()
                .username(req.getUsername())
                .password(encoder.encode(req.getPassword()))
                .email(req.getEmail())
                .nickname(req.getNickname())
                .role(RoleType.ROLE_USER)
                .build();

        userRepository.save(signupUser);
    }
}

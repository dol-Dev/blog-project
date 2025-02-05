package com.doldev.dollog.service.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doldev.dollog.dto.auth.SignupReqDto;
import com.doldev.dollog.entity.RoleType;
import com.doldev.dollog.entity.User;
import com.doldev.dollog.repository.user.UserRepository;

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

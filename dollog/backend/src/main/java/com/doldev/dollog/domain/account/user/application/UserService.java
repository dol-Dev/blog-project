package com.doldev.dollog.domain.account.user.application;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doldev.dollog.domain.account.profile.application.ProfileService;
import com.doldev.dollog.domain.account.profile.entity.Profile;
import com.doldev.dollog.domain.account.roletype.enums.RoleType;
import com.doldev.dollog.domain.account.user.dto.req.EmailUpdateReqDto;
import com.doldev.dollog.domain.account.user.dto.req.PasswordUpdateReqDto;
import com.doldev.dollog.domain.account.user.dto.req.UserSignupReqDto;
import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.account.user.repository.UserRepository;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final BCryptPasswordEncoder encoder;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ProfileService profileSerivce;

    // 회원 가입
    @Transactional
    public void signup(UserSignupReqDto reqDto) {

        // 1. Profile 생성
        Profile profile = profileSerivce.createProfile(reqDto.getNickname());

        // 2. User 생성
        User user = User.builder()
                .username(reqDto.getUsername())
                .password(encoder.encode(reqDto.getPassword()))
                .email(reqDto.getEmail())
                .role(RoleType.ROLE_USER)
                .profile(profile)
                .build();

        userRepository.save(user);
    }

    // 회원탈퇴
    public void withdrawUser(CustomUserDetails userDetails, String accessToken, String refreshToken) {

        String username = userDetails.getUsername();
        String refreshKey = "refresh_" + username;
        String storedRefreshToken = redisTemplate.opsForValue().get(refreshKey);

        // 전달된 refreshToken과 Redis에 저장된 값 비교
        if (!StringUtils.equals(refreshKey, storedRefreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token.");
        }

        redisTemplate.delete(refreshKey);

        userRepository.delete(userDetails.getUser());
    }

    // 일반 회원 이메일 수정
    @Transactional
    public void updateEmail(EmailUpdateReqDto reqDto, CustomUserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUser().getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원 찾기 실패"));

        user.changeEmail(reqDto.getNewEmail());
    }

    // 일바 회원 비밀번호 수정
    @Transactional
    public void updatePassword(PasswordUpdateReqDto reqDto, CustomUserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUser().getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원 찾기 실패"));

        user.changePassword(encoder.encode(reqDto.getNewPassword()));
    }
}
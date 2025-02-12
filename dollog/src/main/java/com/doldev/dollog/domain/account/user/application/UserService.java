package com.doldev.dollog.domain.account.user.application;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doldev.dollog.domain.account.profile.entity.Profile;
import com.doldev.dollog.domain.account.profile.service.ProfileSerivce;
import com.doldev.dollog.domain.account.roletype.enums.RoleType;
import com.doldev.dollog.domain.account.user.dto.req.SignupReqDto;
import com.doldev.dollog.domain.account.user.dto.req.UpdateUserReqDto;
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
    private final ProfileSerivce profileSerivce;

    // 회원 가입
    @Transactional
    public void signup(SignupReqDto req) {

        // 사용자 생성
        User user = User.builder()
                .username(req.getUsername())
                .password(encoder.encode(req.getPassword()))
                .email(req.getEmail())
                .role(RoleType.ROLE_USER)
                .build();

        Profile profile = profileSerivce.createProfile(req.getNickname());
        user.assignProfile(profile);
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

    // 일반 회원 정보 수정
    @Transactional
    public void updateUser(UpdateUserReqDto req,
            CustomUserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUser().getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원 찾기 실패"));

        user.changeEmail(req.getEmail());
        if (StringUtils.isNotBlank(req.getNewPassword())) {
            user.changePassword(encoder.encode(req.getNewPassword()));
        }
    }
}

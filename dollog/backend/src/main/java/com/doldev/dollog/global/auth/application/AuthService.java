package com.doldev.dollog.global.auth.application;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.snsUser.repository.SnsUserRepository;
import com.doldev.dollog.domain.account.user.dto.req.UserLoginReqDto;
import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.account.user.repository.UserRepository;
import com.doldev.dollog.global.auth.dto.res.AuthenticatedUserResDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.auth.service.TokenCookieService;
import com.doldev.dollog.global.auth.service.TokenService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final TokenService tokenService;
    private final TokenCookieService tokenCookieService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final SnsUserRepository snsUserRepository;
    private final JavaMailSender javaMailSender;

    // 로그인
    public void login(UserLoginReqDto reqDto, HttpServletResponse res) {
        // 사용자 조회 및 탈퇴 상태 확인
        Optional<User> userOpt = userRepository.findByUsername(reqDto.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isWithdrawStatus()) { // withdraw_status가 true이면
                throw new IllegalStateException("ACCOUNT_DISABLED");
            }
        } else {
            Optional<SnsUser> snsUserOpt = snsUserRepository.findByUsername(reqDto.getUsername());
            if (snsUserOpt.isPresent()) {
                SnsUser snsUser = snsUserOpt.get();
                if (snsUser.isWithdrawStatus()) {
                    throw new IllegalStateException("ACCOUNT_DISABLED");
                }
            }
        }
        // 탈퇴 상태가 아니라면 정상 로그인 처리
        setBeforeLoginAuthenticationUser(reqDto);
        Map<String, String> tokens = tokenService.generateNewTokens(reqDto.getUsername());
        tokenCookieService.setTokens(res, tokens);
    }

    // 로그아웃
    public void logout(CustomUserDetails userDetails, HttpServletRequest req, HttpServletResponse res) {
        clearUserSession(userDetails, req, res);
    }

    // 회원 탈퇴
    public void withdrawUser(CustomUserDetails userDetails, HttpServletRequest req, HttpServletResponse res) {
        clearUserSession(userDetails, req, res);

        // 탈퇴 유예 기간(7일 -> mysql의 스케쥴러로 관리)
        LocalDateTime withdrawReqAt = LocalDateTime.now();

        if (userDetails.isUser()) {
            User user = userDetails.getUser();
            user.setWithdrawReqAt(withdrawReqAt);
            user.setWithdrawStatus(true);
            userRepository.save(user);
        } else {
            SnsUser snsUser = userDetails.getSnsUser();
            snsUser.setWithdrawReqAt(withdrawReqAt);
            snsUser.setWithdrawStatus(true);
            snsUserRepository.save(snsUser);
        }
    }

    // 회원 탈퇴 시 이메일 검증을 위한 코드 전송
    public String sendCodeMail(String email) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        String authNum = createCode();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(email); // 메일 수신자
            mimeMessageHelper.setSubject("인증을 위한 코드 발송"); // 메일 제목
            mimeMessageHelper.setText("인증코드는 " + authNum + "입니다.");
            javaMailSender.send(mimeMessage);
            return authNum;

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    // 인증번호 및 임시 비밀번호 생성 메서드
    public String createCode() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(4);

            switch (index) {
                // case0,1 : 소문자, 대문자 랜덤생성, deafault: 0~9까지 임의의 숫자 랜덤생성
                case 0:
                    key.append((char) (random.nextInt(26) + 97));
                    break;
                case 1:
                    key.append((char) (random.nextInt(26) + 65));
                    break;
                default:
                    key.append(random.nextInt(9));
            }
        }
        return key.toString();
    }

    // 비활성화 해제
    public void unlockAccount(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isWithdrawStatus()) {
                user.setWithdrawStatus(false);
                user.setWithdrawReqAt(null);
                userRepository.save(user);
            }
            return;
        }

        Optional<SnsUser> snsUserOpt = snsUserRepository.findByUsername(username);
        if (snsUserOpt.isPresent()) {
            SnsUser snsUser = snsUserOpt.get();
            if (snsUser.isWithdrawStatus()) {
                snsUser.setWithdrawStatus(false);
                snsUser.setWithdrawReqAt(null);
                snsUserRepository.save(snsUser);
            }
            return;
        }

        throw new IllegalArgumentException("User not found with username: " + username);
    }

    // 인증된 사용자 정보 반환
    public AuthenticatedUserResDto getUserInfo(CustomUserDetails userDetails) {
        return AuthenticatedUserResDto.from(userDetails);
    }

    // 일반사용자 로그인 요청 시 인증객체 설정
    private void setBeforeLoginAuthenticationUser(UserLoginReqDto reqDto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        reqDto.getUsername(),
                        reqDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // 쿠키에 존재하는 토큰 삭제 무효화 및 레디스에 내역 삭제
    private void clearUserSession(CustomUserDetails userDetails, HttpServletRequest req, HttpServletResponse res) {
        String refreshKey = "refresh_" + userDetails.getUsername();
        String storedRefreshToken = Optional.ofNullable(redisTemplate.opsForValue().get(refreshKey))
                .map(token -> token.substring(7))
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found in Redis."));

        Optional<String> refreshTokenOpt = tokenCookieService.extractRefreshToken(req);
        refreshTokenOpt.ifPresent(r -> {
            if (!StringUtils.equals(r, storedRefreshToken)) {
                throw new IllegalArgumentException("Invalid refresh token.");
            }
        });

        redisTemplate.delete(refreshKey);
        tokenCookieService.clearTokens(res);
    }
}

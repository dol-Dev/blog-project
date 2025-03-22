package com.doldev.dollog.global.auth.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doldev.dollog.domain.account.user.dto.req.UserLoginReqDto;
import com.doldev.dollog.global.auth.application.AuthService;
import com.doldev.dollog.global.auth.dto.res.AuthenticatedUserResDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.dto.ApiResDto;
import com.doldev.dollog.global.validator.CheckLoginValidator;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private final AuthService authService;
        private final CheckLoginValidator checkLoginValidator;

        // 로그인
        @PostMapping("/login")
        public ResponseEntity<ApiResDto<Void>> login(
                        @RequestBody UserLoginReqDto reqDto,
                        HttpServletResponse res) throws BindException {
                BindingResult bindingResult = new BeanPropertyBindingResult(reqDto, "userLoginReqDto");
                checkLoginValidator.validate(reqDto, bindingResult);
                if (bindingResult.hasErrors()) {
                        throw new BindException(bindingResult);
                }
                authService.login(reqDto, res);
                return ResponseEntity.ok(ApiResDto.<Void>builder()
                                .messageCode("SUCCESS_LOGIN")
                                .build());
        }

        // 로그아웃
        @PostMapping("/logout")
        public ResponseEntity<ApiResDto<Void>> logout(
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        HttpServletRequest req,
                        HttpServletResponse res) {
                authService.logout(userDetails, req, res);
                return ResponseEntity.ok(ApiResDto.<Void>builder()
                                .messageCode("LOGOUT_SUCCESS")
                                .build());
        }

        // 회원 탈퇴
        @DeleteMapping("/withdraw")
        public ResponseEntity<ApiResDto<Void>> deleteUser(
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        HttpServletRequest req,
                        HttpServletResponse res) {
                authService.withdrawUser(userDetails, req, res);
                return ResponseEntity.ok(ApiResDto.<Void>builder()
                                .messageCode("WITHDRAW_SUCCESS")
                                .build());
        }

        // 비활성화 해제
        @PostMapping("/unlock")
        public ResponseEntity<ApiResDto<Void>> unlock(@RequestBody Map<String, String> payload) {
                String username = payload.get("username");
                if (username == null || username.trim().isEmpty()) {
                        throw new IllegalArgumentException("Username must be provided.");
                }

                authService.unlockAccount(username);

                return ResponseEntity.ok(ApiResDto.<Void>builder()
                                .messageCode("ACCOUNT_UNLOCKED")
                                .build());
        }

        // 인증된 사용자의 여러 정보 조회
        @GetMapping("/info")
        public ResponseEntity<ApiResDto<AuthenticatedUserResDto>> getUserInfo(
                        @AuthenticationPrincipal CustomUserDetails userDetails) {
                AuthenticatedUserResDto userInfo = authService.getUserInfo(userDetails);
                return ResponseEntity.ok(ApiResDto.<AuthenticatedUserResDto>builder()
                                .messageCode("USER_INFO_FETCH_SUCCESS")
                                .data(userInfo)
                                .build());
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiResDto<Void>> handleAuthException(AuthenticationException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResDto.<Void>builder().messageCode("LOGIN_FAILED").build());
        }
}

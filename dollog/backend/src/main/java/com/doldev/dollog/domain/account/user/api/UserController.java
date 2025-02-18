package com.doldev.dollog.domain.account.user.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doldev.dollog.domain.account.user.application.UserService;
import com.doldev.dollog.domain.account.user.dto.req.UserSignupReqDto;
import com.doldev.dollog.domain.account.user.dto.req.UserUpdateReqDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.dto.ApiResDto;
import com.doldev.dollog.global.validator.CheckSignupValidator;
import com.doldev.dollog.global.validator.CheckUpdateUserValidator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "User API", description = "사용자 관련 API")
@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class UserController {
        private final CheckUpdateUserValidator checkUpdateUserValidator;
        private final CheckSignupValidator checkSignupValidator;
        private final UserService userService;

        // InitBinder를 사용하여 Validator를 등록
        @InitBinder
        public void initBinder(WebDataBinder binder) {
                binder.addValidators(checkSignupValidator);
                binder.addValidators(checkUpdateUserValidator);
        }

        @Operation(summary = "회원가입", description = "사용자가 회원가입을 진행함.")
        @PostMapping("/signup")
        public ResponseEntity<ApiResDto<Void>> signup(
                        @Valid @RequestBody UserSignupReqDto reqDto) {
                userService.signup(reqDto);
                return ResponseEntity.ok()
                                .body(ApiResDto.<Void>builder()
                                                .messageCode("SIGNUP_SUCCESS")
                                                .build());
        }

        @Operation(summary = "회원탈퇴", description = "현재 로그인된 사용자가 회원탈퇴를 진행함.")
        @DeleteMapping("/withdraw")
        public ResponseEntity<ApiResDto<Void>> deleteUser(
                        @Parameter(description = "현재 로그인된 사용자 정보", hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
                        @Parameter(description = "Access Token 값", required = true) @CookieValue("accessToken") String accessToken,
                        @Parameter(description = "Refresh Token 값", required = true) @CookieValue("refreshToken") String refreshToken) {
                userService.withdrawUser(userDetails, accessToken, refreshToken);
                return ResponseEntity.ok()
                                .body(ApiResDto.<Void>builder()
                                                .messageCode("WITHDRAW_SUCCESS")
                                                .build());
        }

        @Operation(summary = "회원 정보 수정", description = "사용자가 정보를 수정함.")
        @PutMapping
        public ResponseEntity<ApiResDto<Void>> updateUser(
                        @Parameter(description = "현재 로그인된 사용자 정보", hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
                        @Parameter(description = "수정할 사용자 정보") @Valid @RequestBody UserUpdateReqDto reqDto) {
                userService.updateUser(reqDto, userDetails);
                return ResponseEntity.ok()
                                .body(ApiResDto.<Void>builder()
                                                .messageCode("USER_INFO_UPDATE_SUCCESS")
                                                .build());
        }
}
package com.doldev.dollog.domain.account.user.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doldev.dollog.domain.account.user.application.UserService;
import com.doldev.dollog.domain.account.user.dto.req.EmailUpdateReqDto;
import com.doldev.dollog.domain.account.user.dto.req.PasswordUpdateReqDto;
import com.doldev.dollog.domain.account.user.dto.req.UserSignupReqDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.dto.ApiResDto;
import com.doldev.dollog.global.validator.CheckSignupValidator;
import com.doldev.dollog.global.validator.CheckUpdateUserEmailValidator;
import com.doldev.dollog.global.validator.CheckUpdateUserPasswordValidator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "User API", description = "사용자 관련 API")
@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class UserController {

        private final CheckUpdateUserEmailValidator checkUpdateUserEmailValidator;
        private final CheckUpdateUserPasswordValidator checkUpdateUserPasswordValidator;
        private final CheckSignupValidator checkSignupValidator;
        private final UserService userService;

        @Operation(summary = "회원가입", description = "사용자가 회원가입을 진행함.")
        @PostMapping("/signup")
        public ResponseEntity<ApiResDto<Void>> signup(@RequestBody UserSignupReqDto reqDto) throws BindException {

                BindingResult bindingResult = new BeanPropertyBindingResult(reqDto, "userSignupReqDto");
                checkSignupValidator.validate(reqDto, bindingResult);
                if (bindingResult.hasErrors()) {
                        throw new BindException(bindingResult);
                }

                userService.signup(reqDto);
                return ResponseEntity.ok(ApiResDto.<Void>builder()
                                .messageCode("SIGNUP_SUCCESS")
                                .build());
        }

        @Operation(summary = "회원탈퇴", description = "현재 로그인된 사용자가 회원탈퇴를 진행함.")
        @DeleteMapping("/withdraw")
        public ResponseEntity<ApiResDto<Void>> deleteUser(
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        @CookieValue("accessToken") String accessToken,
                        @CookieValue("refreshToken") String refreshToken) {
                userService.withdrawUser(userDetails, accessToken, refreshToken);
                return ResponseEntity.ok(ApiResDto.<Void>builder()
                                .messageCode("WITHDRAW_SUCCESS")
                                .build());
        }

        @Operation(summary = "이메일 수정", description = "사용자 이메일을 수정함.")
        @PutMapping("/email")
        public ResponseEntity<ApiResDto<Void>> updateEmail(
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
                        @RequestBody EmailUpdateReqDto reqDto) throws BindException {
                BindingResult bindingResult = new BeanPropertyBindingResult(reqDto, "emailUpdateReqDto");
                checkUpdateUserEmailValidator.validate(reqDto, bindingResult);
                if (bindingResult.hasErrors()) {
                        throw new BindException(bindingResult);
                }

                userService.updateEmail(reqDto, userDetails);
                return ResponseEntity.ok(ApiResDto.<Void>builder()
                                .messageCode("EMAIL_UPDATE_SUCCESS")
                                .build());
        }

        @Operation(summary = "비밀번호 수정", description = "사용자 비밀번호를 수정함.")
        @PutMapping("/password")
        public ResponseEntity<ApiResDto<Void>> updatePassword(
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
                        @RequestBody PasswordUpdateReqDto reqDto) throws BindException {
                BindingResult bindingResult = new BeanPropertyBindingResult(reqDto, "passwordUpdateReqDto");
                checkUpdateUserPasswordValidator.validate(reqDto, bindingResult);
                if (bindingResult.hasErrors()) {
                        throw new BindException(bindingResult);
                }

                userService.updatePassword(reqDto, userDetails);
                return ResponseEntity.ok(ApiResDto.<Void>builder()
                                .messageCode("PASSWORD_UPDATE_SUCCESS")
                                .build());
        }
}

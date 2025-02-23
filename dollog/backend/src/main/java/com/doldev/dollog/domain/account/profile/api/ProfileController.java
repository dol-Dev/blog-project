package com.doldev.dollog.domain.account.profile.api;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.doldev.dollog.domain.account.profile.application.ProfileService;
import com.doldev.dollog.domain.account.profile.dto.req.BlogNameUpdateReqDto;
import com.doldev.dollog.domain.account.profile.dto.req.NicknameUpdateReqDto;
import com.doldev.dollog.domain.account.profile.dto.req.ProfileUpdateReqDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.dto.ApiResDto;
import com.doldev.dollog.global.validator.CheckUpdateAvatarValidator;
import com.doldev.dollog.global.validator.CheckUpdateBlogNameValidator;
import com.doldev.dollog.global.validator.CheckUpdateNicknameValidator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ProfileController {

    private final CheckUpdateNicknameValidator checkUpdateNicknameValidator;
    private final CheckUpdateAvatarValidator checkUpdateAvatarValidator;
    private final CheckUpdateBlogNameValidator checkUpdateBlogNameValidator;
    private final ProfileService profileService;

    // InitBinder를 사용하여 Validator를 등록
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(checkUpdateNicknameValidator);
        binder.addValidators(checkUpdateAvatarValidator);
        binder.addValidators(checkUpdateBlogNameValidator);
    }


    // 프로필 수정
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResDto<Void>> updateUser(
            @Valid @ModelAttribute ProfileUpdateReqDto req,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        profileService.updateProfile(req, avatarFile, userDetails);
        return ResponseEntity.ok().body(ApiResDto.<Void>builder().messageCode("PROFILE_UPDATE_SUCCESS").build());
    }

    // 닉네임 수정
    @PutMapping(value = "/nickname")
    public ResponseEntity<ApiResDto<Void>> updateNickname(
            @Valid @ModelAttribute NicknameUpdateReqDto req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        profileService.updateNickname(req, userDetails);
        return ResponseEntity.ok().body(ApiResDto.<Void>builder().messageCode("NICKNAME_UPDATE_SUCCESS").build());
    }

    // 아바타 수정
    @PutMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResDto<Void>> updateAvatar(
            @RequestPart(value = "avatarFile") MultipartFile avatarFile,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {

        profileService.updateAvatar(avatarFile, userDetails);
        return ResponseEntity.ok().body(ApiResDto.<Void>builder().messageCode("AVATAR_UPDATE_SUCCESS").build());
    }

    // 블로그 이름 수정
    @PutMapping(value = "/blogName")
    public ResponseEntity<ApiResDto<Void>> updateBlogName(
            @Valid @ModelAttribute BlogNameUpdateReqDto req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        profileService.updateBlogName(req, userDetails);
        return ResponseEntity.ok().body(ApiResDto.<Void>builder().messageCode("BLOG_NAME_UPDATE_SUCCESS").build());
    }
}
package com.doldev.dollog.domain.account.profile.api;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.doldev.dollog.domain.account.profile.application.ProfileService;
import com.doldev.dollog.domain.account.profile.dto.req.BlogNameUpdateReqDto;
import com.doldev.dollog.domain.account.profile.dto.req.NicknameUpdateReqDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.dto.ApiResDto;
import com.doldev.dollog.global.validator.CheckUpdateAvatarValidator;
import com.doldev.dollog.global.validator.CheckUpdateBlogNameValidator;
import com.doldev.dollog.global.validator.CheckUpdateNicknameValidator;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final CheckUpdateNicknameValidator checkUpdateNicknameValidator;
    private final CheckUpdateAvatarValidator checkUpdateAvatarValidator;
    private final CheckUpdateBlogNameValidator checkUpdateBlogNameValidator;
    private final ProfileService profileService;

    // 닉네임 수정
    @PutMapping("/nickname")
    public ResponseEntity<ApiResDto<Void>> updateNickname(
            @RequestBody NicknameUpdateReqDto reqDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws BindException {
        BindingResult bindingResult = new BeanPropertyBindingResult(reqDto, "nicknameUpdateReqDto");
        checkUpdateNicknameValidator.validate(reqDto, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        profileService.updateNickname(reqDto, userDetails);
        return ResponseEntity.ok().body(ApiResDto.<Void>builder()
                .messageCode("NICKNAME_UPDATE_SUCCESS")
                .build());
    }

    // 아바타 수정
    @PutMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResDto<Void>> updateAvatar(
            @RequestPart(value = "avatarFile") MultipartFile avatarFile,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws BindException, IOException {
        BindingResult bindingResult = new BeanPropertyBindingResult(avatarFile, "avatarFile");
        checkUpdateAvatarValidator.validate(avatarFile, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        profileService.updateAvatar(avatarFile, userDetails);
        return ResponseEntity.ok().body(ApiResDto.<Void>builder()
                .messageCode("AVATAR_UPDATE_SUCCESS")
                .build());
    }

    // 블로그 이름 수정
    @PutMapping("/blogName")
    public ResponseEntity<ApiResDto<Void>> updateBlogName(
            @RequestBody BlogNameUpdateReqDto reqDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws BindException {
        BindingResult bindingResult = new BeanPropertyBindingResult(reqDto, "blogNameUpdateReqDto");
        checkUpdateBlogNameValidator.validate(reqDto, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        profileService.updateBlogName(reqDto, userDetails);
        return ResponseEntity.ok().body(ApiResDto.<Void>builder()
                .messageCode("BLOG_NAME_UPDATE_SUCCESS")
                .build());
    }
}

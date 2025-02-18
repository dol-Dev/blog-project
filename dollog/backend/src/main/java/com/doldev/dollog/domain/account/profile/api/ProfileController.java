package com.doldev.dollog.domain.account.profile.api;

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

import com.doldev.dollog.domain.account.profile.application.ProfileSerivce;
import com.doldev.dollog.domain.account.profile.dto.req.ProfileUpdateReqDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.dto.ApiResDto;
import com.doldev.dollog.global.validator.CheckUpdateProfileValidator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ProfileController {

    private final CheckUpdateProfileValidator checkUpdateProfileValidator;
    private final ProfileSerivce profileService;

    // InitBinder를 사용하여 Validator를 등록o
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(checkUpdateProfileValidator);
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

}
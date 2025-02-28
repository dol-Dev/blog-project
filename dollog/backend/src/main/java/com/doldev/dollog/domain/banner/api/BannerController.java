package com.doldev.dollog.domain.banner.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doldev.dollog.domain.banner.application.BannerService;
import com.doldev.dollog.domain.banner.dto.req.BannerReqDto;
import com.doldev.dollog.domain.banner.dto.res.LayoutRelatedInfoResDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.dto.ApiResDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    // 배너 생성
    @PostMapping
    public ResponseEntity<ApiResDto<?>> createBanner(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody BannerReqDto bannerRequest) {
        bannerService.createBanner(userDetails, bannerRequest);
        return ResponseEntity.ok()
                .body(ApiResDto.builder().messageCode("BANNER_CREATION_SUCCESS").build());
    }

    // 인증된 사용자의 배너 조회(로그인o)
    @GetMapping
    public ResponseEntity<ApiResDto<?>> getLayoutInfoByPrincipal(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        LayoutRelatedInfoResDto resDto = bannerService.findLayoutInfoByPrincipal(userDetails);
        if (resDto == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResDto.builder().messageCode("LAYOUT_INFO_FETCH_FAILED").build());
        }
        return ResponseEntity.ok()
                .body(ApiResDto.builder().messageCode("LAYOUT_INFO_FETCH_SUCCESS").data(resDto).build());
    }

    // 해당 닉네임에 해당하는 사용자의 배너 조회(로그인 유무x)
    @GetMapping("/{nickname}")
    public ResponseEntity<ApiResDto<?>> getLayoutInfoByNickname(@PathVariable("nickname") String nickname) {
        LayoutRelatedInfoResDto resDto = bannerService.findLayoutInfoByNickname(nickname);
        if (resDto == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResDto.builder().messageCode("LAYOUT_INFO_FETCH_BY_NICKNAME_FAILED").build());
        }
        return ResponseEntity.ok()
                .body(ApiResDto.builder().messageCode("LAYOUT_INFO_FETCH_BY_NICKNAME_SUCCESS").data(resDto).build());
    }
}

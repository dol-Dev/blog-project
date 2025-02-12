package com.doldev.dollog.domain.account.snsUser.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.doldev.dollog.domain.account.snsUser.application.SnsService;
import com.doldev.dollog.domain.account.snsUser.enums.SnsType;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SnsController {

    private final SnsService snsService;

    @GetMapping("/api/oauth2/kakao/callback")
    public String kakaoLogin(@RequestParam("code") String code)
            throws JsonProcessingException {
        snsService.process(SnsType.KAKAO, code);
        return "redirect:http://localhost:3000";
    }

    @GetMapping("/api/oauth2/naver/callback")
    public String naverLogin(@RequestParam("code") String code)
            throws JsonProcessingException {
        snsService.process(SnsType.NAVER, code);
        return "redirect:http://localhost:3000";
    }
}
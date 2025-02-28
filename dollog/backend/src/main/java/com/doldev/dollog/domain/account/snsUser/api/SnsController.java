package com.doldev.dollog.domain.account.snsUser.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.doldev.dollog.domain.account.snsUser.application.SnsService;
import com.doldev.dollog.domain.account.snsUser.enums.SnsType;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class SnsController {

    private final SnsService snsService;

    @GetMapping("/kakao/callback")
    public String kakaoLogin(@RequestParam("code") String code, HttpServletRequest req, HttpServletResponse res)
            throws JsonProcessingException {
        snsService.process(SnsType.KAKAO, code, req, res);
        return "redirect:http://localhost:3000";
    }

    @GetMapping("/naver/callback")
    public String naverLogin(@RequestParam("code") String code, HttpServletRequest req, HttpServletResponse res)
            throws JsonProcessingException {
        snsService.process(SnsType.NAVER, code, req, res);
        return "redirect:http://localhost:3000";
    }
}
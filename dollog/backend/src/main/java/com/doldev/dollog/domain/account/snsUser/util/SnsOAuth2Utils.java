package com.doldev.dollog.domain.account.snsUser.util;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.doldev.dollog.domain.account.snsUser.enums.SnsType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnsOAuth2Utils {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getAccessToken(String code, SnsType snsType, String clientId, String clientSecret,
            String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        SecureRandom random = new SecureRandom();
        String state = new BigInteger(130, random).toString(32);

        params.add("state", state);
        params.add("grant_type", snsType.getGrantType());
        params.add("client_id", clientId);
        params.add("code", code);

        if (StringUtils.equals("KAKAO", snsType.getProvider())) {
            params.add("redirect_uri", redirectUri);
        }

        if (snsType.getClientSecretKey() != null && clientSecret != null) {
            params.add(snsType.getClientSecretKey(), clientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                snsType.getTokenUrl(),
                request,
                String.class);
                
        return parseAccessToken(response.getBody());
    }

    private String parseAccessToken(String responseBody) {
        try {
            JsonNode accessTokenNode = objectMapper.readTree(responseBody).get("access_token");
            if (accessTokenNode == null) {
                throw new RuntimeException("access_token not found in response: " + responseBody);
            }
            return accessTokenNode.asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse access token", e);
        }
    }

    public String gerUsername(String accessToken, SnsType snsType) {
        log.info("gerUsername start");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                snsType.getUserInfoUrl(),
                HttpMethod.GET,
                request,
                String.class);

        return parseUsername(response.getBody(), snsType);
    }

    private String parseUsername(String responseBody, SnsType snsType) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            if (snsType == SnsType.NAVER) {
                rootNode = rootNode.get("response");
            }
            return rootNode.get("id").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse user unique ID", e);
        }
    }
}
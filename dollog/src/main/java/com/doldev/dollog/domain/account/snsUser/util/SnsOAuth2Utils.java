package com.doldev.dollog.domain.account.snsUser.util;

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


@Component
@RequiredArgsConstructor
public class SnsOAuth2Utils {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getAccessToken(String code, SnsType snsType, String clientId, String clientSecret, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", snsType.getGrantType());
        params.add("client_id", clientId);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        if(snsType.getClientSecretKey() != null && clientSecret != null) {
            params.add(snsType.getClientSecretKey(), clientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            snsType.getTokenUrl(), 
            request, 
            String.class
        );

        return parseAccessToken(response.getBody());
    }

    private String parseAccessToken(String responseBody) {
        try {
            return objectMapper.readTree(responseBody).get("access_token").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse access token", e);
        }
    }

    public String gerSnsIdentifier(String accessToken, SnsType snsType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            snsType.getUserInfoUrl(),
            HttpMethod.GET,
            request,
            String.class
        );

        return parseSnsIdentifier(response.getBody(), snsType);
    }

    private String parseSnsIdentifier(String responseBody, SnsType snsType) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            if(snsType == SnsType.NAVER) {
                rootNode = rootNode.get("response");
            }
            return rootNode.get("id").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse user unique ID", e);
        }
    }
}
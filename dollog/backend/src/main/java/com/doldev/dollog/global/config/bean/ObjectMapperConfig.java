package com.doldev.dollog.global.config.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ObjectMapperConfig { // ObjectMapper를 다른 곳에서도 사용할 예정이므로 Config로 설정

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

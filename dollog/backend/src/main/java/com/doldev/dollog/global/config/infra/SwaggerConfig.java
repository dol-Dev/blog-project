package com.doldev.dollog.global.config.infra;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(title = "dollog 프로젝트 API 명세서", description = "dollog 프로젝트에 사용되는 API 명세서", version = "v1"))
@Configuration
public class SwaggerConfig {
    @Bean
    GroupedOpenApi openApi() {
        String[] paths = {"/api**"};

        return GroupedOpenApi.builder()
        .group("doldev's Swager-v1")
        .pathsToMatch(paths)
        .build();
    }
}
    

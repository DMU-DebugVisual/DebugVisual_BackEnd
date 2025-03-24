package com.dmu.debug_visual.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("디버깅 시각화 알고리즘 API")
                        .description("React + Spring Boot 기반 디버깅 시각화 프로젝트 API 문서입니다.")
                        .version("v1.0.0"));
    }
}


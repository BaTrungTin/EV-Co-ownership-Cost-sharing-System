package com.evcoownership.coowner.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP) // Kiểu là HTTP
                                        .scheme("bearer")               // Scheme là "bearer"
                                        .bearerFormat("JWT")            // Định dạng là "JWT"
                                        .in(SecurityScheme.In.HEADER)   // Nằm trong Header
                                        .name("Authorization")))        // Tên của header

                .info(new Info()
                                .title("EV Co-ownership System API")
                                .version("1.0.0")
                                .description("API documentation for the Electric Vehicle Co-ownership & Cost-sharing System.")
                );
    }
}
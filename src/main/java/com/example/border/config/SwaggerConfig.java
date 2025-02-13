package com.example.border.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${openapi.url}")
    private String apiUrl;

    private static final String API_KEY = "Bearer Token ";

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8080/");
        server.setDescription("URL-адрес сервера в среде local");

        Server apiServer = new Server();
        apiServer.setUrl(apiUrl);
        apiServer.setDescription("URL-адрес сервера в среде Prod");


        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(API_KEY, apiKeySecurityScheme()))
                .info(new Info().title("HR System API").version("1.0.0"))
                .security(Collections.singletonList(new SecurityRequirement().addList(API_KEY)))
                .servers(List.of(server, apiServer));
    }

    public SecurityScheme apiKeySecurityScheme() {
        return new SecurityScheme()
                .name("Auth API")
                .description("Пожалуйста, вставьте токен!")
                .in(SecurityScheme.In.HEADER)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer");
    }
}

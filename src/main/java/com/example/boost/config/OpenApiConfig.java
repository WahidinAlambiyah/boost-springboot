package com.example.boost.config;

import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        String schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title("Boost API").version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .tags(List.of(
                        new Tag().name("Authentication"),
                        new Tag().name("Password Management"),
                        new Tag().name("User Management"),
                        new Tag().name("Role Management"),
                        new Tag().name("Permission Management"),
                        new Tag().name("Audit Log")
                ));
    }

    @Bean
    public OpenApiCustomizer cleanApi() {
        return openApi -> openApi.getPaths().values().forEach(pathItem -> {
            pathItem.setHead(null);   // remove HEAD
            pathItem.setOptions(null); // optional
        });
    }

    // @Bean
    // public GroupedOpenApi userApi() {
    //     return GroupedOpenApi.builder()
    //             .group("User APIs")
    //             .pathsToMatch("/api/users/**")
    //             .build();
    // }
}

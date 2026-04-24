package com.example.boost.common.config;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OpenApiCustomizer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        String schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title("Boost API").version("v1").description("""
                            REST API untuk sistem manajemen user, role, dan permission.

                            ## Authentication
                            Gunakan JWT Bearer Token:
                            `Authorization: Bearer <token>`

                            ## Error Format
                            Semua error menggunakan format standar JSON.
                            """)
                        .contact(new Contact()
                                .name("API Support")
                                .email("halo@alambiyah.com")))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .tags(List.of(
                        new Tag().name("1. Authentication"),
                        new Tag().name("2. Password Management"),
                        new Tag().name("3. User Management"),
                        new Tag().name("4. Role Management"),
                        new Tag().name("5. Permission Management"),
                        new Tag().name("6. Audit Log")
                ));
    }

    @Bean
    @Order(1)
    public OpenApiCustomizer cleanApi() {
        return openApi -> openApi.getPaths().values().forEach(pathItem -> {
            pathItem.setHead(null);   // remove HEAD
            pathItem.setOptions(null); // optional
        });
    }

    @Bean
    @Order(2)
    public OpenApiCustomizer sortTagsAlphabetically() {
        return openApi -> {
            List<String> order = List.of(
                    "1. Authentication",
                    "2. Password Management",
                    "3. User Management",
                    "4. Role Management",
                    "5. Permission Management",
                    "6. Audit Log"
            );

            openApi.setTags(
                    openApi.getTags().stream()
                            .sorted((t1, t2) -> Integer.compare(
                            order.indexOf(t1.getName()),
                            order.indexOf(t2.getName())
                    ))
                            .toList()
            );
            // openApi.setTags(openApi.getTags()
            //         .stream()
            //         .sorted(Comparator.comparing(tag -> tag.getName().toLowerCase()))
            //         .collect(Collectors.toList()));
        };
    }

    // @Bean
    // @Order(3)
    // public OpenApiCustomizer sortOperations() {
    //     return openApi -> openApi.getPaths().values().forEach(pathItem -> {
    //         if (pathItem.readOperations() != null) {
    //             pathItem.readOperations().sort((o1, o2)
    //                     -> o1.getSummary().compareToIgnoreCase(o2.getSummary())
    //             );
    //         }
    //     });
    // }
}

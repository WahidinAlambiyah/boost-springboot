package com.example.boost.common.config;

import java.util.List;

import io.swagger.v3.oas.models.Components;
import org.springdoc.core.customizers.OpenApiCustomizer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
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
                .components(new Components()
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
                        new Tag().name("6. Audit Log"),
                        new Tag().name("Academy Management"),
                        new Tag().name("Academy Location Management"),
                        new Tag().name("Coach Profile Management"),
                        new Tag().name("7. Catalog"),
                        new Tag().name("8. Scheduling"),
                        new Tag().name("9. Enrollment"),
                        new Tag().name("10. Attendance"),
                        new Tag().name("11. Billing"),
                        new Tag().name("12. Notification"),
                        new Tag().name("13. Assessment Skills"),
                        new Tag().name("14. Assessment"),
                        new Tag().name("15. Reporting")
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
    public OpenApiCustomizer sortTagsByDefinedOrder() {
        return openApi -> {
            List<String> order = List.of(
                    "1. Authentication",
                    "2. Password Management",
                    "3. User Management",
                    "4. Role Management",
                    "5. Permission Management",
                    "6. Audit Log",
                    "Academy Management",
                    "Academy Location Management",
                    "Coach Profile Management",
                    "7. Catalog",
                    "8. Scheduling",
                    "9. Enrollment",
                    "10. Attendance",
                    "11. Billing",
                    "12. Notification",
                    "13. Assessment Skills",
                    "14. Assessment",
                    "15. Reporting"
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

    @Bean
    @Order(3)
    public OpenApiCustomizer applyDefaultAuthErrorResponses() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(this::ensureAuthErrorResponses)
        );
    }

    private void ensureAuthErrorResponses(Operation operation) {
        if (operation.getSecurity() != null && operation.getSecurity().isEmpty()) {
            return;
        }

        if (operation.getResponses() == null) {
            operation.setResponses(new io.swagger.v3.oas.models.responses.ApiResponses());
        }

        operation.getResponses().computeIfAbsent("401", key ->
                new ApiResponse()
                        .description("Unauthorized")
                        .content(authErrorContent(401, "Unauthorized")));

        operation.getResponses().computeIfAbsent("403", key ->
                new ApiResponse()
                        .description("Forbidden")
                        .content(authErrorContent(403, "Forbidden")));
    }

    private Content authErrorContent(int status, String message) {
        return new Content().addMediaType(
                "application/json",
                new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/ApiResponse"))
                        .addExamples("default", new Example().value(String.format("""
                                {
                                  "status": %d,
                                  "message": "%s",
                                  "data": null
                                }
                                """, status, message)))
        );
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

package com.example.workorder.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Work Order Management API",
                version = "1.0",
                description = "API documentation for managing divisions and projects with role-scoped access."
        ),
        security = {
                @SecurityRequirement(name = "roleHeader"),
                @SecurityRequirement(name = "divisionHeader")
        }
)
@SecurityScheme(
        name = "roleHeader",
        type = SecuritySchemeType.APIKEY,
        paramName = "X-ROLE",
        in = SecuritySchemeIn.HEADER
)
@SecurityScheme(
        name = "divisionHeader",
        type = SecuritySchemeType.APIKEY,
        paramName = "X-DIVISION-ID",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}

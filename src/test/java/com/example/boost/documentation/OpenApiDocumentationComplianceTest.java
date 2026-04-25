package com.example.boost.documentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:boost-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "spring.datasource.hikari.schema=PUBLIC",
        "spring.jpa.properties.hibernate.default_schema=fastworks_springboot",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:schema.sql",
        "spring.datasource.hikari.connection-init-sql=CREATE SCHEMA IF NOT EXISTS fastworks_springboot",
        "management.health.redis.enabled=false",
        "management.health.mail.enabled=false"
})
@AutoConfigureMockMvc
class OpenApiDocumentationComplianceTest {

    private static final Set<String> LEGACY_CONTROLLERS_WITHOUT_STRICT_DOC_RULE = Set.of(
            "com.example.boost.controller.MetricsDashboardController",
            "com.example.boost.iam.api.AuditLogController",
            "com.example.boost.iam.api.PasswordResetController",
            "com.example.boost.iam.api.PermissionController",
            "com.example.boost.iam.api.RoleController",
            "com.example.boost.iam.api.UserController"
    );

    private static final Set<String> LEGACY_METHODS_WITHOUT_OPERATION_SUMMARY = Set.of(
            "com.example.boost.iam.api.AuthController#authHeadCheck"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    @Test
    void shouldGenerateOpenApiSpecFileDuringTestBuild() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

        String openApiJson = result.getResponse().getContentAsString();
        JsonNode openApi = objectMapper.readTree(openApiJson);

        assertThat(openApi.path("openapi").asText()).isNotBlank();
        assertThat(openApi.path("paths").isObject()).isTrue();

        writeSpec(openApiJson);
    }

    @Test
    void shouldRequireTagAndOperationSummaryForNewControllers() {
        Set<Class<?>> restControllers = applicationContext.getBeansWithAnnotation(RestController.class)
                .values()
                .stream()
                .map(AopUtils::getTargetClass)
                .filter(controllerClass -> controllerClass.getPackageName().startsWith("com.example.boost"))
                .collect(Collectors.toCollection(HashSet::new));

        List<String> violations = new ArrayList<>();

        for (Class<?> controllerClass : restControllers) {
            String className = controllerClass.getName();

            if (LEGACY_CONTROLLERS_WITHOUT_STRICT_DOC_RULE.contains(className)) {
                continue;
            }

            Tag tag = AnnotatedElementUtils.findMergedAnnotation(controllerClass, Tag.class);
            if (tag == null || tag.name().isBlank()) {
                violations.add(className + " is missing @Tag(name = ...)");
            }

            for (Method method : controllerClass.getDeclaredMethods()) {
                if (!isEndpointMethod(method) || isHeadOrOptionsOnlyMethod(method)) {
                    continue;
                }

                String methodIdentifier = className + "#" + method.getName();
                if (LEGACY_METHODS_WITHOUT_OPERATION_SUMMARY.contains(methodIdentifier)) {
                    continue;
                }

                Operation operation = AnnotatedElementUtils.findMergedAnnotation(method, Operation.class);
                if (operation == null || operation.summary().isBlank()) {
                    violations.add(methodIdentifier + " is missing @Operation(summary = ...)");
                }
            }
        }

        violations.sort(Comparator.naturalOrder());
        assertThat(violations)
                .withFailMessage("OpenAPI documentation violations:\n%s", String.join("\n", violations))
                .isEmpty();
    }

    private static boolean isEndpointMethod(Method method) {
        return AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class)
                || AnnotatedElementUtils.hasAnnotation(method, GetMapping.class)
                || AnnotatedElementUtils.hasAnnotation(method, PostMapping.class)
                || AnnotatedElementUtils.hasAnnotation(method, PutMapping.class)
                || AnnotatedElementUtils.hasAnnotation(method, DeleteMapping.class)
                || AnnotatedElementUtils.hasAnnotation(method, PatchMapping.class);
    }

    private static boolean isHeadOrOptionsOnlyMethod(Method method) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
        if (requestMapping == null || requestMapping.method().length == 0) {
            return false;
        }
        return Arrays.stream(requestMapping.method())
                .allMatch(httpMethod -> httpMethod == RequestMethod.HEAD || httpMethod == RequestMethod.OPTIONS);
    }

    private static void writeSpec(String openApiJson) throws IOException {
        Path outputPath = Path.of("target", "openapi", "openapi.json");
        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, openApiJson);
    }
}

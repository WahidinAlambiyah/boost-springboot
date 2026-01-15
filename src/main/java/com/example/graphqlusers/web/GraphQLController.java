package com.example.graphqlusers.web;

import com.example.graphqlusers.app.AppException;
import com.example.graphqlusers.app.ErrorCodes;
import com.example.graphqlusers.auth.AuthContext;
import com.example.graphqlusers.auth.JwtService;
import com.example.graphqlusers.graphql.GraphQLContext;
import com.example.graphqlusers.graphql.GraphQLProvider;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GraphQLController {
    private final GraphQL graphQL;
    private final GraphQLProvider graphQLProvider;
    private final JwtService jwtService;

    public GraphQLController(GraphQLProvider graphQLProvider, JwtService jwtService) {
        this.graphQLProvider = graphQLProvider;
        this.graphQL = graphQLProvider.graphQL();
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/graphql", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> handle(@RequestBody(required = false) GraphQLRequest request,
                                                      @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (request == null || request.query() == null || request.query().isBlank()) {
                return ResponseEntity.badRequest().body(graphQLError("Query is required", ErrorCodes.BAD_REQUEST));
            }
            AuthContext authContext = resolveAuth(authorization);
            GraphQLContext context = new GraphQLContext(authContext, graphQLProvider.buildRegistry());
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                    .query(request.query())
                    .operationName(request.operationName())
                    .variables(request.variables() == null ? Collections.emptyMap() : request.variables())
                    .context(context)
                    .dataLoaderRegistry(context.dataLoaderRegistry())
                    .build();
            ExecutionResult result = graphQL.execute(executionInput);
            return ResponseEntity.ok(result.toSpecification());
        } catch (AppException appException) {
            return ResponseEntity.ok(graphQLError(appException.getMessage(), appException.getCode()));
        } catch (Exception e) {
            return ResponseEntity.ok(graphQLError("Internal server error", "INTERNAL"));
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(graphQLError("Invalid JSON body", ErrorCodes.BAD_REQUEST));
    }

    private AuthContext resolveAuth(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring("Bearer ".length());
        try {
            return jwtService.parse(token);
        } catch (Exception e) {
            throw new AppException(ErrorCodes.UNAUTHORIZED, "Invalid token");
        }
    }

    private Map<String, Object> graphQLError(String message, String code) {
        // default non-null
        String safeMessage = (message == null || message.isBlank())
                ? "Unexpected error"
                : message;

        String safeCode = (code == null || code.isBlank())
                ? "INTERNAL"
                : code;

        Map<String, Object> extensions = new LinkedHashMap<>();
        extensions.put("code", safeCode);

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("message", safeMessage);
        error.put("extensions", extensions);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("data", null);
        resp.put("errors", List.of(error));
        return resp;
    }
}

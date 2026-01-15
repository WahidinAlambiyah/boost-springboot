package com.example.graphqlusers.web;

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
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GraphQLController {
    private final GraphQL graphQL;

    public GraphQLController(GraphQLProvider graphQLProvider) {
        this.graphQL = graphQLProvider.graphQL();
    }

    @PostMapping(value = "/graphql", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> handle(@RequestBody(required = false) GraphQLRequest request) {
        try {
            if (request == null || request.query() == null || request.query().isBlank()) {
                return ResponseEntity.badRequest().body(graphQLError("Query is required"));
            }
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                    .query(request.query())
                    .operationName(request.operationName())
                    .variables(request.variables() == null ? Collections.emptyMap() : request.variables())
                    .build();
            ExecutionResult result = graphQL.execute(executionInput);
            return ResponseEntity.ok(result.toSpecification());
        } catch (Exception e) {
            return ResponseEntity.ok(graphQLError("Internal server error"));
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(graphQLError("Invalid JSON body"));
    }

    private Map<String, Object> graphQLError(String message) {
        String safeMessage = (message == null || message.isBlank())
                ? "Unexpected error"
                : message;

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("message", safeMessage);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("data", null);
        resp.put("errors", List.of(error));
        return resp;
    }
}

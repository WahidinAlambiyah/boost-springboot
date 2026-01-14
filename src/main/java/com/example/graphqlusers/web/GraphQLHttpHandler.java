package com.example.graphqlusers.web;

import com.example.graphqlusers.app.AppException;
import com.example.graphqlusers.app.ErrorCodes;
import com.example.graphqlusers.auth.AuthContext;
import com.example.graphqlusers.auth.JwtService;
import com.example.graphqlusers.graphql.GraphQLContext;
import com.example.graphqlusers.graphql.GraphQLProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.io.IOException;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;

public class GraphQLHttpHandler implements HttpHandler {
    private final GraphQL graphQL;
    private final GraphQLProvider graphQLProvider;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public GraphQLHttpHandler(GraphQLProvider graphQLProvider, JwtService jwtService, ObjectMapper objectMapper) {
        this.graphQLProvider = graphQLProvider;
        this.graphQL = graphQLProvider.graphQL();
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (!exchange.getRequestMethod().equalToString("POST")) {
            exchange.setStatusCode(405);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send("{\"error\":\"Method Not Allowed\"}");
            return;
        }
        exchange.startBlocking();
        try {
            GraphQLRequest request = readRequest(exchange);
            AuthContext authContext = resolveAuth(exchange);
            GraphQLContext context = new GraphQLContext(authContext, graphQLProvider.buildRegistry());
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                    .query(request.query())
                    .operationName(request.operationName())
                    .variables(request.variables() == null ? Collections.emptyMap() : request.variables())
                    .context(context)
                    .dataLoaderRegistry(context.dataLoaderRegistry())
                    .build();
            ExecutionResult result = graphQL.execute(executionInput);
            String responseBody = objectMapper.writeValueAsString(result.toSpecification());
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(responseBody);
        } catch (AppException appException) {
            sendGraphQLError(exchange, appException.getMessage(), appException.getCode());
        } catch (Exception e) {
            sendGraphQLError(exchange, "Internal server error", "INTERNAL");
        }
    }

    private GraphQLRequest readRequest(HttpServerExchange exchange) throws IOException {
        return objectMapper.readValue(exchange.getInputStream(), GraphQLRequest.class);
    }

    private AuthContext resolveAuth(HttpServerExchange exchange) {
        Deque<String> header = exchange.getRequestHeaders().get("Authorization");
        if (header == null || header.isEmpty()) {
            return null;
        }
        String value = header.getFirst();
        if (value == null || !value.startsWith("Bearer ")) {
            return null;
        }
        String token = value.substring("Bearer ".length());
        try {
            return jwtService.parse(token);
        } catch (Exception e) {
            throw new AppException(ErrorCodes.UNAUTHORIZED, "Invalid token");
        }
    }

    private void sendGraphQLError(HttpServerExchange exchange, String message, String code) throws IOException {
        Map<String, Object> error = Map.of(
                "message", message,
                "extensions", Map.of("code", code)
        );
        Map<String, Object> payload = Map.of(
                "data", null,
                "errors", java.util.List.of(error)
        );
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(objectMapper.writeValueAsString(payload));
    }
}

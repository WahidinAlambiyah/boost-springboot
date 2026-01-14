package com.example.graphqlusers.web;

import java.util.Map;

public record GraphQLRequest(
        String query,
        Map<String, Object> variables,
        String operationName
) {
}

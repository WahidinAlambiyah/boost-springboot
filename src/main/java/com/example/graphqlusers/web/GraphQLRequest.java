package com.example.graphqlusers.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GraphQLRequest(
        String query,
        Map<String, Object> variables,
        String operationName
) {
}

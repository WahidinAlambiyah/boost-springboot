package com.example.graphqlusers.graphql;

import com.example.graphqlusers.app.AppException;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GraphQLExceptionHandler implements DataFetcherExceptionHandler {
    @Override
    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(DataFetcherExceptionHandlerParameters parameters) {
        Throwable exception = parameters.getException();
        GraphQLError error;
        if (exception instanceof AppException appException) {
            error = GraphqlErrorBuilder.newError()
                    .message(appException.getMessage())
                    .location(parameters.getSourceLocation())
                    .path(parameters.getPath())
                    .errorType(ErrorType.DataFetchingException)
                    .extensions(Map.of("code", appException.getCode()))
                    .build();
        } else {
            error = GraphqlErrorBuilder.newError()
                    .message("Internal server error")
                    .location(parameters.getSourceLocation())
                    .path(parameters.getPath())
                    .errorType(ErrorType.DataFetchingException)
                    .extensions(Map.of("code", "INTERNAL"))
                    .build();
        }
        return CompletableFuture.completedFuture(DataFetcherExceptionHandlerResult.newResult().error(error).build());
    }
}

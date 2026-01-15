package com.example.graphqlusers.graphql;

import com.example.graphqlusers.service.UserService;
import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class GraphQLProvider {
    private final GraphQL graphQL;

    public GraphQLProvider(UserService userService) {
        this.graphQL = buildGraphQL(userService);
    }

    public GraphQL graphQL() {
        return graphQL;
    }

    private GraphQL buildGraphQL(UserService userService) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser()
                .parse(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("schema.graphqls"), StandardCharsets.UTF_8));
        RuntimeWiring wiring = buildWiring(userService);
        GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
        return GraphQL.newGraphQL(schema)
                .queryExecutionStrategy(new AsyncExecutionStrategy())
                .build();
    }

    private RuntimeWiring buildWiring(UserService userService) {
        QueryDataFetchers queryFetchers = new QueryDataFetchers(userService);
        return RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring -> typeWiring
                        .dataFetcher("users", queryFetchers.users())
                        .dataFetcher("user", queryFetchers.user())
                )
                .build();
    }
}

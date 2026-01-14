package com.example.graphqlusers.graphql;

import com.example.graphqlusers.repository.UserRepository;
import com.example.graphqlusers.service.AuthService;
import com.example.graphqlusers.service.UserService;
import graphql.GraphQL;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.AsyncExecutionStrategy;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Component;

@Component
public class GraphQLProvider {
    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthService authService;
    private final GraphQL graphQL;

    public GraphQLProvider(UserRepository userRepository, UserService userService, AuthService authService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.authService = authService;
        this.graphQL = buildGraphQL();
    }

    public GraphQL graphQL() {
        return graphQL;
    }

    private GraphQL buildGraphQL() {
        TypeDefinitionRegistry typeRegistry = new SchemaParser()
                .parse(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("schema.graphqls"), StandardCharsets.UTF_8));
        RuntimeWiring wiring = buildWiring();
        GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
        return GraphQL.newGraphQL(schema)
                .queryExecutionStrategy(new AsyncExecutionStrategy(new GraphQLExceptionHandler()))
                .instrumentation(new MaxQueryDepthInstrumentation(8))
                .build();
    }

    public DataLoaderRegistry buildRegistry() {
        DataLoader<UUID, com.example.graphqlusers.app.User> userLoader = DataLoader.newMappedDataLoader(keys ->
                CompletableFuture.supplyAsync(() -> {
                    var map = new java.util.HashMap<UUID, com.example.graphqlusers.app.User>();
                    for (UUID key : keys) {
                        map.put(key, userRepository.findById(key).orElse(null));
                    }
                    return map;
                })
        );
        DataLoaderRegistry registry = new DataLoaderRegistry();
        registry.register("userLoader", userLoader);
        return registry;
    }

    private RuntimeWiring buildWiring() {
        QueryDataFetchers queryFetchers = new QueryDataFetchers(userService);
        MutationDataFetchers mutationFetchers = new MutationDataFetchers(authService, userService);
        return RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring -> typeWiring
                        .dataFetcher("me", queryFetchers.me())
                        .dataFetcher("user", queryFetchers.user())
                        .dataFetcher("users", queryFetchers.users())
                )
                .type("Mutation", typeWiring -> typeWiring
                        .dataFetcher("register", mutationFetchers.register())
                        .dataFetcher("login", mutationFetchers.login())
                        .dataFetcher("refreshToken", mutationFetchers.refreshToken())
                        .dataFetcher("logout", mutationFetchers.logout())
                        .dataFetcher("createUser", mutationFetchers.createUser())
                        .dataFetcher("updateUser", mutationFetchers.updateUser())
                        .dataFetcher("deleteUser", mutationFetchers.deleteUser())
                )
                .build();
    }
}

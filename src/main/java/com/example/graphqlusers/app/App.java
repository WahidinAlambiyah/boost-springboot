package com.example.graphqlusers.app;

import com.example.graphqlusers.auth.JwtService;
import com.example.graphqlusers.config.AppConfig;
import com.example.graphqlusers.db.DataSourceFactory;
import com.example.graphqlusers.db.MigrationRunner;
import com.example.graphqlusers.graphql.GraphQLProvider;
import com.example.graphqlusers.redis.RedisFactory;
import com.example.graphqlusers.redis.RefreshTokenStore;
import com.example.graphqlusers.redis.RedisRefreshTokenStore;
import com.example.graphqlusers.repository.PostgresUserRepository;
import com.example.graphqlusers.repository.UserRepository;
import com.example.graphqlusers.service.AuthService;
import com.example.graphqlusers.service.UserService;
import com.example.graphqlusers.web.CorsHandler;
import com.example.graphqlusers.web.GraphQLHttpHandler;
import com.example.graphqlusers.web.GraphiQLHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.Undertow;
import io.undertow.Handlers;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.PathHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import javax.sql.DataSource;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        AppConfig config = AppConfig.fromEnv();
        DataSource dataSource = DataSourceFactory.create(config.jdbcUrl(), config.dbUser(), config.dbPassword());
        MigrationRunner.migrate(dataSource);

        JedisPool jedisPool = RedisFactory.create(config.redisHost(), config.redisPort());
        RefreshTokenStore refreshTokenStore = new RedisRefreshTokenStore(jedisPool, config.refreshTtl());
        JwtService jwtService = new JwtService(config.jwtSecret(), config.jwtTtl());

        UserRepository userRepository = new PostgresUserRepository(dataSource);
        UserService userService = new UserService(userRepository);
        AuthService authService = new AuthService(userRepository, jwtService, refreshTokenStore);

        GraphQLProvider graphQLProvider = new GraphQLProvider(userRepository, userService, authService);
        ObjectMapper objectMapper = new ObjectMapper();

        GraphQLHttpHandler graphQLHttpHandler = new GraphQLHttpHandler(graphQLProvider, jwtService, objectMapper);

        PathHandler routes = Handlers.path()
                .addExactPath("/graphql", new CorsHandler(new BlockingHandler(graphQLHttpHandler)))
                .addExactPath("/graphiql", new CorsHandler(new GraphiQLHandler()));

        Undertow server = Undertow.builder()
                .addHttpListener(config.appPort(), "0.0.0.0")
                .setHandler(routes)
                .build();
        server.start();
        LOGGER.info("Server started on port {}", config.appPort());
    }
}

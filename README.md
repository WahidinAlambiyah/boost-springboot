# java-graphql-users

Backend GraphQL Users API using Java 21, Spring Boot, graphql-java 25, Undertow, PostgreSQL, Redis, JWT, and Flyway.

## Prerequisites
- Java 21 (LTS)
- Maven 3.9+
- Docker + Docker Compose

## Environment Variables (defaults)
```
APP_PORT=8080
JDBC_URL=jdbc:postgresql://localhost:5432/appdb
DB_USER=app
DB_PASSWORD=app
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=dev-secret-change-me-min-32-chars
JWT_TTL_MINUTES=15
REFRESH_TTL_DAYS=30
```

## Run
1) Start dependencies
```
docker compose up -d
```

2) Check Java 21
```
mvn -v
```

3) Build
```
mvn clean package
```

4) Run
```
java -jar target/java-graphql-users.jar
```

5) Open GraphiQL
```
http://localhost:8080/graphiql
```

## GraphQL Endpoint
- `POST /graphql`
- Body: `{ "query": "...", "variables": { ... }, "operationName": "..." }`

## Sample Queries & Mutations

### Register
```
mutation Register($input: RegisterInput!) {
  register(input: $input) {
    accessToken
    refreshToken
    user { id username email roles }
  }
}
```
Variables:
```
{
  "input": {
    "username": "alice",
    "email": "alice@example.com",
    "fullName": "Alice Doe",
    "password": "password123"
  }
}
```

### Login
```
mutation {
  login(usernameOrEmail: "alice", password: "password123") {
    accessToken
    refreshToken
    user { id username roles }
  }
}
```

### Me (Authorization: Bearer <accessToken>)
```
query {
  me { id username email roles }
}
```

### Create User (ADMIN only)
```
mutation {
  createUser(input: {
    username: "admin",
    email: "admin@example.com",
    fullName: "Admin",
    password: "password123",
    roles: [ADMIN]
  }) {
    id
    username
    roles
  }
}
```

### List Users (ADMIN only)
```
query {
  users(page: 0, size: 10) {
    total
    items { id username email roles }
  }
}
```

### Update User (ADMIN or owner)
```
mutation {
  updateUser(id: "<USER_ID>", input: { fullName: "Alice Updated" }) {
    id
    fullName
    updatedAt
  }
}
```

### Refresh Token
```
mutation {
  refreshToken(refreshToken: "<REFRESH_TOKEN>") {
    accessToken
    refreshToken
    user { id username }
  }
}
```

### Logout
```
mutation {
  logout(refreshToken: "<REFRESH_TOKEN>")
}
```

## Notes
- Access tokens are HS256 JWTs with 15-minute default TTL.
- Refresh tokens are stored in Redis and rotated on refresh.
- GraphQL errors include `extensions.code` (BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, NOT_FOUND).

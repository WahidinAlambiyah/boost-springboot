# boost-springboot-graphql-starter

Starter kit GraphQL Spring Boot yang sederhana untuk belajar. Fokus hanya pada query untuk mengambil data user.

## Prerequisites
- Java 21 (LTS)
- Maven 3.9+

## Run
1) Build
```
mvn clean package
```

2) Run
```
java -jar target/java-graphql-users.jar
```

3) Open GraphiQL
```
http://localhost:8080/graphiql
```

> GraphiQL diaktifkan lewat `spring.graphql.graphiql.enabled: true` di `application.yml`.
> Halaman GraphiQL memakai aset lokal agar terhindar dari error CORS saat mengambil asset dari CDN eksternal.

## GraphQL Schema
Simpan schema di `src/main/resources/graphql/schema.graphqls` agar otomatis terdeteksi oleh Spring GraphQL.

## GraphQL Endpoint
- `POST /graphql`
- Body: `{ "query": "...", "variables": { ... }, "operationName": "..." }`

## Sample Queries

### List Users
```
query {
  users {
    id
    username
    email
    fullName
  }
}
```

### Single User
```
query {
  user(id: "1") {
    id
    username
    email
    fullName
  }
}
```

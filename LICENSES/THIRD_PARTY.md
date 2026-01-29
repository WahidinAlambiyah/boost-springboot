# Third-Party Licenses

This project depends on third-party libraries. Their licenses must be reviewed and complied with separately. Do **not** assume a license unless verified.

## How to update
1. Generate dependency tree:
   ```bash
   mvn -q dependency:tree > /tmp/dependency-tree.txt
   ```
2. Optionally generate a license report (verify plugin compatibility for your environment):
   ```bash
   mvn -q -DskipTests=true org.codehaus.mojo:license-maven-plugin:2.4.0:aggregate-add-third-party
   ```
3. Verify each dependency's license from its official repository or published POM.
4. Update the table below with accurate license names and links.

## Dependency License List (verify)

| Dependency | Version | License | Notes |
| --- | --- | --- | --- |
| org.springframework.boot:spring-boot-starter-web | 3.3.2 (parent) | Verify | Spring Boot starter aggregate. |
| org.springframework.boot:spring-boot-starter-validation | 3.3.2 (parent) | Verify | Starter aggregate. |
| org.springframework.boot:spring-boot-starter-data-jpa | 3.3.2 (parent) | Verify | Starter aggregate. |
| org.springframework.boot:spring-boot-starter-security | 3.3.2 (parent) | Verify | Starter aggregate. |
| org.springframework.boot:spring-boot-starter-data-redis | 3.3.2 (parent) | Verify | Starter aggregate. |
| org.springframework.boot:spring-boot-starter-actuator | 3.3.2 (parent) | Verify | Starter aggregate. |
| org.springdoc:springdoc-openapi-starter-webmvc-ui | 2.5.0 | Verify | OpenAPI UI. |
| org.postgresql:postgresql | Managed by Spring Boot | Verify | PostgreSQL JDBC driver. |
| org.liquibase:liquibase-core | Managed by Spring Boot | Verify | Database migrations. |
| io.jsonwebtoken:jjwt-api | 0.12.5 | Verify | JWT library. |
| io.jsonwebtoken:jjwt-impl | 0.12.5 | Verify | JWT runtime. |
| io.jsonwebtoken:jjwt-jackson | 0.12.5 | Verify | JWT Jackson integration. |
| org.projectlombok:lombok | Managed by Spring Boot | Verify | Provided scope. |
| org.springframework.boot:spring-boot-starter-test | 3.3.2 (parent) | Verify | Test dependencies. |
| org.springframework.security:spring-security-test | Managed by Spring Boot | Verify | Test dependencies. |

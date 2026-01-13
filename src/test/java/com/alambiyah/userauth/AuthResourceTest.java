package com.alambiyah.userauth;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class AuthResourceTest {

    @Test
    void registerLoginAndAccessMe() {
        String username = "user1";
        String email = "user1@example.com";
        String password = "User12345!";

        given()
                .contentType(ContentType.JSON)
                .body("{" +
                        "\"username\":\"" + username + "\"," +
                        "\"email\":\"" + email + "\"," +
                        "\"fullName\":\"User One\"," +
                        "\"password\":\"" + password + "\"}")
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());

        JsonPath login = given()
                .contentType(ContentType.JSON)
                .body("{" +
                        "\"usernameOrEmail\":\"" + username + "\"," +
                        "\"password\":\"" + password + "\"}")
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        String accessToken = login.getString("accessToken");

        given()
                .auth().oauth2(accessToken)
                .when()
                .get("/api/me")
                .then()
                .statusCode(200)
                .body("username", equalTo(username));

        given()
                .auth().oauth2(accessToken)
                .when()
                .get("/api/users")
                .then()
                .statusCode(403);
    }

    @Test
    void adminCanAccessUsers() {
        JsonPath adminLogin = given()
                .contentType(ContentType.JSON)
                .body("{\"usernameOrEmail\":\"admin\",\"password\":\"Admin123!\"}")
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        String adminToken = adminLogin.getString("accessToken");

        given()
                .auth().oauth2(adminToken)
                .when()
                .get("/api/users")
                .then()
                .statusCode(200);
    }

    @Test
    void refreshTokenRotationRevokesOldToken() {
        String username = "rotator";
        String email = "rotator@example.com";
        String password = "Rotate123!";

        given()
                .contentType(ContentType.JSON)
                .body("{" +
                        "\"username\":\"" + username + "\"," +
                        "\"email\":\"" + email + "\"," +
                        "\"fullName\":\"Token Rotator\"," +
                        "\"password\":\"" + password + "\"}")
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201);

        JsonPath login = given()
                .contentType(ContentType.JSON)
                .body("{" +
                        "\"usernameOrEmail\":\"" + username + "\"," +
                        "\"password\":\"" + password + "\"}")
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        String refreshToken = login.getString("refreshToken");

        JsonPath refreshed = given()
                .contentType(ContentType.JSON)
                .body("{\"refreshToken\":\"" + refreshToken + "\"}")
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        String newRefreshToken = refreshed.getString("refreshToken");

        given()
                .contentType(ContentType.JSON)
                .body("{\"refreshToken\":\"" + refreshToken + "\"}")
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(401);

        given()
                .contentType(ContentType.JSON)
                .body("{\"refreshToken\":\"" + newRefreshToken + "\"}")
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(200);
    }
}

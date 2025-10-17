package com.vic.historyservice.Controller;


import com.vic.historyservice.BaseIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HistoryControllerIT extends BaseIntegrationTest {

    @Test
    @Order(1)
    void shouldReturnWalletHistory_WhenWalletExists() {
        String walletId = "wallet-123"; // use a known test wallet id

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/wallets/{walletId}/history", walletId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", not(empty()))
                .body("[0].walletId", equalTo(walletId))
                .body("[0].event_type", notNullValue())
                .body("[0].amount", greaterThan(0f));
    }

    @Test
    @Order(2)
    void shouldReturn404_WhenWalletDoesNotExist() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/wallets/{walletId}/history", "non-existent-wallet")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(3)
    void shouldReturnUserActivityHistory_WhenUserExists() {
        String userId = "user-456"; // replace with a valid test user

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/users/{userId}/activity", userId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", not(empty()))
                .body("[0].user_id", equalTo(userId))
                .body("[0].event_type", notNullValue());
    }

    @Test
    @Order(4)
    void shouldReturn404_WhenUserDoesNotExist() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/users/{userId}/activity", "ghost-user")
                .then()
                .statusCode(404);
    }
}

package com.vic.walletservice.Controller;

import com.vic.walletservice.AbstractIT;
import com.vic.walletservice.Dtos.FundWalletRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class WalletControllerTest extends AbstractIT {

    // Helper record for transfers
    record TransferRequest(String fromUserId, String toWalletId, BigDecimal amount) {}

    @Test
    void createAndFundWallet_ShouldSucceed() {
        String userId = "user-" + System.currentTimeMillis();

        // 1️⃣ Create a new wallet
        String walletId =
                given()
                        .contentType(ContentType.JSON)
                        .body(userId)
                        .when()
                        .post("/api/wallets/{userId}", userId)
                        .then()
                        .statusCode(200)
                        .extract().asString();

        // 2️⃣ Fund the wallet
        given()
                .contentType(ContentType.JSON)
                .body(new FundWalletRequest(userId, new BigDecimal("300.00")))
                .when()
                .post("/api/wallets/{walletId}/fund", walletId)
                .then()
                .statusCode(200)
                .body("status", equalTo("COMPLETED"));

        // 3️⃣ Verify wallet balance
        given()
                .pathParam("walletId", walletId)
                .when()
                .get("/api/wallets/{walletId}")
                .then()
                .statusCode(200)
                .body(equalTo("300.00"));
    }

    @Test
    void transferFunds_ShouldFail_WhenInsufficientBalance() {
        String userFrom = "userFrom-" + System.currentTimeMillis();
        String userTo = "userTo-" + System.currentTimeMillis();

        // Create and fund source wallet with 100
        String walletFrom =
                given()
                        .contentType(ContentType.JSON)
                        .body(userFrom)
                        .when()
                        .post("/api/wallets/{userId}", userFrom)
                        .then()
                        .statusCode(200)
                        .extract().asString();

        given()
                .contentType(ContentType.JSON)
                .body(new FundWalletRequest(userFrom, new BigDecimal("100.00")))
                .when()
                .post("/api/wallets/{walletId}/fund", walletFrom)
                .then()
                .statusCode(200);

        // Create destination wallet
        String walletTo =
                given()
                        .contentType(ContentType.JSON)
                        .body(userTo)
                        .when()
                        .post("/api/wallets/{userId}", userTo)
                        .then()
                        .statusCode(200)
                        .extract().asString();

        // Attempt a transfer exceeding balance
        TransferRequest transferRequest = new TransferRequest(userFrom, walletTo, new BigDecimal("500.00"));

        given()
                .contentType(ContentType.JSON)
                .body(transferRequest)
                .when()
                .post("/api/wallets/{walletId}/transfer", walletFrom)
                .then()
                .statusCode(200)
                .body(equalTo("FAILED"));
    }

    @Test
    void getUserWallets_ShouldReturnWallets() {
        String userId = "user-" + System.currentTimeMillis();

        // Create two wallets for the same user
        String wallet1 =
                given()
                        .pathParam("userId", userId)
                        .when()
                        .post("/api/wallets/{userId}", userId)
                        .then()
                        .statusCode(200)
                        .extract().asString();

        String wallet2 =
                given()
                        .pathParam("userId", userId)
                        .when()
                        .post("/api/wallets/{userId}", userId)
                        .then()
                        .statusCode(200)
                        .extract().asString();

        // Fund the first wallet
        given()
                .contentType(ContentType.JSON)
                .body(new FundWalletRequest(userId, new BigDecimal("50.00")))
                .when()
                .post("/api/wallets/{walletId}/fund", wallet1)
                .then()
                .statusCode(200);

        // Fetch all wallets for the user
        given()
                .pathParam("userId", userId)
                .when()
                .get("/api/users/{userId}/wallets")
                .then()
                .statusCode(200)
                .body("[0].userId", equalTo(userId))
                .body("[1].userId", equalTo(userId))
                .body("[0].balance", notNullValue())
                .body("[1].balance", notNullValue());
    }

    @Test
    void fundWallet_ShouldFail_WhenMissingAmount() {
        String userId = "user-" + System.currentTimeMillis();

        String walletId =
                given()
                        .contentType(ContentType.JSON)
                        .body(userId)
                        .when()
                        .post("/api/wallets/{userId}", userId)
                        .then()
                        .statusCode(200)
                        .extract().asString();

        // Send empty amount to trigger validation
        given()
                .contentType(ContentType.JSON)
                .body("{\"userId\": \"" + userId + "\"}")
                .when()
                .post("/api/wallets/{walletId}/fund", walletId)
                .then()
                .statusCode(400)
                .body("amount", equalTo("Amount is required"));
    }
}

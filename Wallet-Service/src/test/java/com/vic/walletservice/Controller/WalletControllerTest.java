package com.vic.walletservice.Controller;

import com.vic.walletservice.AbstractIT;
import com.vic.walletservice.Dtos.FundWalletRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.math.BigDecimal;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;



@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WalletControllerTest extends AbstractIT {

    String userId = "user-123";

    static String walletA;
    static String walletB;
    record TransferRequest(String fromUserId, String toWalletId, BigDecimal amount) {}

    @Test
    void createAndFundWallet() {
        // 1️⃣ Create a new wallet for user
        String walletId =
                given()
                        .contentType(ContentType.JSON)
                        .body(userId)
                        .when()
                        .post("/api/wallets")
                        .then()
                        .statusCode(200)
                .extract().asString();
        // 2️⃣ Fund the wallet
        given()
                .contentType(ContentType.JSON)
                .body(new FundWalletRequest(userId, new BigDecimal("300.00") ))
                .when()
                .post("/api/wallets/{walletId}/fund", walletId)
                .then()
                .statusCode(200)
                .body("status", equalTo("COMPLETED"));

        // 3️⃣ Check wallet balance
        given()
                .pathParam("walletId", walletId)
                .when()
                .get("/wallets/{walletId}/balance")
                .then()
                .statusCode(200)
                .body(equalTo("150.00"));
    }

    @Test
    @Order(3)
    void transferFundsBetweenWallets_ShouldFail_WhenInsufficientFunds() {
        var request = new TransferRequest("userA", walletB, new BigDecimal("5000.00"));

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/wallets/{walletId}/transfer", walletA)
                .then()
                .statusCode(200)
                .body(equalTo("FAILED"));
    }

    @Test
    @Order(4)
    void getUserWallets_ShouldReturnAllWalletsForUser() {
        given()
                .pathParam("userId", "userA")
                .when()
                .get("/wallets/users/{userId}/wallets")
                .then()
                .statusCode(200)
                .body("[0].userId", equalTo("userA"))
                .body("[0].balance", notNullValue());
    }
}
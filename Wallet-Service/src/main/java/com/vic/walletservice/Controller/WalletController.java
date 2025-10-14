package com.vic.walletservice.Controller;

import com.vic.walletservice.Dtos.CreateWalletRequest;
import com.vic.walletservice.Dtos.FundWalletRequest;
import com.vic.walletservice.Dtos.TransferRequest;
import com.vic.walletservice.Dtos.WalletResponse;
import com.vic.walletservice.Enums.TransactionStatus;
import com.vic.walletservice.Models.Wallet;
import com.vic.walletservice.Services.walletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Wallet Controller", description = "APIs for managing digital wallets and transactions")
@RestController
@RequestMapping("/api")
public class WalletController {

    private final walletService walletService;

    public WalletController(walletService walletService) {
        this.walletService = walletService;
    }

    @Operation(
            summary = "Create a new wallet",
            description = "Creates a new wallet for a user based on the provided details.",
            requestBody = @RequestBody(
                    required = true,
                    description = "Wallet creation details",
                    content = @Content(
                            schema = @Schema(implementation = CreateWalletRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "userId": "12345"
                                   
                                    }
                                    """)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Wallet created successfully",
                            content = @Content(schema = @Schema(implementation = WalletResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "walletId": "wallet-001",
                                              "userId": "12345",
                                              "currency": "USD",
                                              "balance": 0.00
                                            }
                                            """))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
            }
    )
    @PostMapping("/wallets")
    public ResponseEntity<String> createWallet(@RequestBody String userId) {
        String walletResponse = walletService.createWallet(userId);
        return ResponseEntity.ok(walletResponse);
    }

    @Operation(
            summary = "Fund a wallet",
            description = "Funds the specified wallet with a given amount.",
            parameters = @Parameter(name = "walletId", description = "The ID of the wallet to fund", required = true),
            requestBody = @RequestBody(
                    required = true,
                    description = "Funding details",
                    content = @Content(
                            schema = @Schema(implementation = FundWalletRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "userId": "12345",
                                      "amount": 100.50
                                    }
                                    """)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Wallet funded successfully",
                            content = @Content(schema = @Schema(implementation = TransactionStatus.class),
                                    examples = @ExampleObject(value = "\"SUCCESS\""))),
                    @ApiResponse(responseCode = "404", description = "Wallet not found", content = @Content)
            }
    )
    @PostMapping("/wallets/{walletId}/fund")
    public ResponseEntity<TransactionStatus> fundWallet(
            @PathVariable String walletId,
            @RequestBody FundWalletRequest fundWalletRequest
    ) {
        TransactionStatus status = walletService.fundWallet(
                walletId,
                fundWalletRequest.userId(),
                fundWalletRequest.amount()
        );
        return ResponseEntity.ok(status);
    }

    @Operation(
            summary = "Transfer funds between wallets",
            description = "Transfers funds from one wallet to another.",
            parameters = @Parameter(name = "walletId", description = "The source wallet ID", required = true),
            requestBody = @RequestBody(
                    required = true,
                    description = "Transfer details",
                    content = @Content(
                            schema = @Schema(implementation = TransferRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "fromUserId": "12345",
                                      "toWalletId": "wallet-002",
                                      "amount": 25.00
                                    }
                                    """)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transfer successful",
                            content = @Content(schema = @Schema(implementation = TransactionStatus.class),
                                    examples = @ExampleObject(value = "\"SUCCESS\""))),
                    @ApiResponse(responseCode = "400", description = "Insufficient balance or invalid request", content = @Content)
            }
    )
    @PostMapping("/wallets/{walletId}/transfer")
    public ResponseEntity<TransactionStatus> transferWallet(
            @PathVariable String walletId,
            @RequestBody TransferRequest transferRequest
    ) {
        TransactionStatus status = walletService.transferFunds(
                walletId,
                transferRequest.fromUserId(),
                transferRequest.toWalletId(),
                transferRequest.amount()
        );
        return ResponseEntity.ok(status);
    }

    @Operation(
            summary = "Get wallet balance",
            description = "Retrieves the current balance of a wallet by its ID.",
            parameters = @Parameter(name = "walletId", description = "Wallet ID", required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Balance retrieved successfully",
                            content = @Content(schema = @Schema(implementation = BigDecimal.class),
                                    examples = @ExampleObject(value = "250.75"))),
                    @ApiResponse(responseCode = "404", description = "Wallet not found", content = @Content)
            }
    )
    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<BigDecimal> getWalletBalance(@PathVariable String walletId) {
        return ResponseEntity.ok(walletService.getBalance(walletId));
    }

    @Operation(
            summary = "Get all wallets of a user",
            description = "Fetches all wallets associated with a specific user.",
            parameters = @Parameter(name = "userId", description = "The user's ID", required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Wallets retrieved successfully",
                            content = @Content(schema = @Schema(implementation = Wallet.class),
                                    examples = @ExampleObject(value = """
                                            [
                                              {
                                                "walletId": "wallet-001",
                                                "userId": "12345",
                                                "currency": "USD",
                                                "balance": 250.75
                                              },
                                              {
                                                "walletId": "wallet-002",
                                                "userId": "12345",
                                                "currency": "EUR",
                                                "balance": 99.00
                                              }
                                            ]
                                            """))),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    @GetMapping("/users/{userId}/wallets")
    public ResponseEntity<List<Wallet>> getUserWallets(@PathVariable String userId) {
        return ResponseEntity.ok(walletService.getUserWallets(userId));
    }
}

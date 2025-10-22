package tn.esprit.piboursebackend.Player.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Player.DTOs.TransactionCreateDTO;
import tn.esprit.piboursebackend.Player.DTOs.TransactionDTO;
import tn.esprit.piboursebackend.Player.Exceptions.ErrorResponse;
import tn.esprit.piboursebackend.Player.Services.ITransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for managing player transactions (buy, sell, deposit, withdraw)")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final ITransactionService transactionService;

    @Operation(
        summary = "Get all transactions",
        description = "Retrieve a list of all transactions in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of transactions",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionDTO.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        List<TransactionDTO> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @Operation(
        summary = "Get transaction by ID",
        description = "Retrieve a specific transaction by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved transaction",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Transaction not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(
            @Parameter(description = "ID of the transaction to retrieve", required = true)
            @PathVariable Long id) {
        TransactionDTO transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    @Operation(
        summary = "Get transactions by player ID",
        description = "Retrieve all transactions for a specific player"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved player transactions",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Player not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByPlayerId(
            @Parameter(description = "ID of the player", required = true)
            @PathVariable Long playerId) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByPlayerId(playerId);
        return ResponseEntity.ok(transactions);
    }

    @Operation(
        summary = "Create a new transaction",
        description = "Create a new transaction for a player. Transaction types: BUY, SELL, DEPOSIT, WITHDRAW. Amount must be positive."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Transaction created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Player not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(
            @Parameter(description = "Transaction data to create", required = true)
            @Valid @RequestBody TransactionCreateDTO transactionCreateDTO) {
        TransactionDTO createdTransaction = transactionService.createTransaction(transactionCreateDTO);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Delete a transaction",
        description = "Delete a transaction from the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Transaction deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Transaction not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "ID of the transaction to delete", required = true)
            @PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}


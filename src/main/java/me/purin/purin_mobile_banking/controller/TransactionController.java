package me.purin.purin_mobile_banking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.purin.purin_mobile_banking.dto.request.TransactionCreateRequestDto;
import me.purin.purin_mobile_banking.dto.response.TransactionResponseDto;
import me.purin.purin_mobile_banking.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDto> transfer(
            @RequestHeader("X-User-Id") UUID initiatedBy,
            @Valid @RequestBody TransactionCreateRequestDto request) {
        TransactionResponseDto result = transactionService.transfer(initiatedBy, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDto> getById(@PathVariable UUID transactionId) {
        return ResponseEntity.ok(transactionService.getById(transactionId));
    }
}

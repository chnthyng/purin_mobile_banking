package me.purin.purin_mobile_banking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.purin.purin_mobile_banking.dto.request.BankAccountCreateRequestDto;
import me.purin.purin_mobile_banking.dto.response.BankAccountResponseDto;
import me.purin.purin_mobile_banking.service.BankAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    public ResponseEntity<BankAccountResponseDto> linkAccount(
            @PathVariable UUID userId,
            @Valid @RequestBody BankAccountCreateRequestDto request) {
        BankAccountResponseDto created = bankAccountService.linkAccount(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<BankAccountResponseDto>> getAccountsForUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(bankAccountService.getAccountsForUser(userId));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<BankAccountResponseDto> getById(
            @PathVariable UUID userId,
            @PathVariable UUID accountId) {
        return ResponseEntity.ok(bankAccountService.getById(accountId));
    }
}

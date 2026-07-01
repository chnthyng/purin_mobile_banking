package me.purin.purin_mobile_banking.service;

import lombok.RequiredArgsConstructor;
import me.purin.purin_mobile_banking.dto.request.BankAccountCreateRequestDto;
import me.purin.purin_mobile_banking.dto.response.BankAccountResponseDto;
import me.purin.purin_mobile_banking.entity.BankAccount;
import me.purin.purin_mobile_banking.exception.ResourceNotFoundException;
import me.purin.purin_mobile_banking.mapper.BankAccountMapper;
import me.purin.purin_mobile_banking.repository.BankAccountRepository;
import me.purin.purin_mobile_banking.security.CryptoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final CryptoService cryptoService;
    private final BankAccountMapper bankAccountMapper;

    public BankAccountResponseDto linkAccount(UUID userId, BankAccountCreateRequestDto request) {
        String accountNumberHash = cryptoService.hmacHash(request.accountNumber());

        BankAccount account = BankAccount.builder()
                .userId(userId)
                // TODO: replace with KMS-encrypted ciphertext before persisting.
                .accountNumber(request.accountNumber())
                .accountNumberHash(accountNumberHash)
                .bankCode(request.bankCode())
                .bankName(request.bankName())
                .accountType(request.accountType())
                .currency(request.currency() != null ? request.currency() : "THB")
                .isPrimary(request.isPrimary())
                .build();

        account = bankAccountRepository.save(account);
        return bankAccountMapper.toResponseDto(account);
    }

    @Transactional(readOnly = true)
    public List<BankAccountResponseDto> getAccountsForUser(UUID userId) {
        return bankAccountRepository.findByUserId(userId).stream()
                .map(bankAccountMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BankAccountResponseDto getById(UUID accountId) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
        return bankAccountMapper.toResponseDto(account);
    }
}

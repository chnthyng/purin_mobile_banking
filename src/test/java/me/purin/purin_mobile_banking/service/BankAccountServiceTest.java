package me.purin.purin_mobile_banking.service;

import me.purin.purin_mobile_banking.TestDataFactory;
import me.purin.purin_mobile_banking.dto.request.BankAccountCreateRequestDto;
import me.purin.purin_mobile_banking.dto.response.BankAccountResponseDto;
import me.purin.purin_mobile_banking.entity.BankAccount;
import me.purin.purin_mobile_banking.enums.AccountType;
import me.purin.purin_mobile_banking.exception.ResourceNotFoundException;
import me.purin.purin_mobile_banking.mapper.BankAccountMapper;
import me.purin.purin_mobile_banking.repository.BankAccountRepository;
import me.purin.purin_mobile_banking.security.CryptoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankAccountService")
class BankAccountServiceTest {

    @Mock BankAccountRepository bankAccountRepository;
    @Mock CryptoService cryptoService;
    @Mock BankAccountMapper bankAccountMapper;

    @InjectMocks BankAccountService bankAccountService;

    @Test
    @DisplayName("linkAccount: สำเร็จ — บันทึกบัญชีพร้อม accountNumberHash")
    void linkAccount_success() {
        BankAccountCreateRequestDto request = TestDataFactory.buildAccountCreateRequest();
        BankAccount saved = TestDataFactory.buildActiveAccount(
                TestDataFactory.ACCOUNT_ID, TestDataFactory.USER_ID, BigDecimal.ZERO);
        BankAccountResponseDto dto = buildResponseDto(saved);

        when(cryptoService.hmacHash(request.accountNumber())).thenReturn("HASH[0011122233341]");
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(saved);
        when(bankAccountMapper.toResponseDto(saved)).thenReturn(dto);

        BankAccountResponseDto result = bankAccountService.linkAccount(TestDataFactory.USER_ID, request);

        var captor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getAccountNumberHash()).isEqualTo("HASH[0011122233341]");
        assertThat(captor.getValue().getUserId()).isEqualTo(TestDataFactory.USER_ID);
        assertThat(result.accountId()).isEqualTo(TestDataFactory.ACCOUNT_ID);
    }

    @Test
    @DisplayName("linkAccount: currency null — default เป็น THB")
    void linkAccount_nullCurrency_defaultsTHB() {
        BankAccountCreateRequestDto request = new BankAccountCreateRequestDto(
                "0011122233341", "SCB", "Siam Commercial Bank", AccountType.SAVINGS, null, true);
        BankAccount saved = TestDataFactory.buildActiveAccount(
                TestDataFactory.ACCOUNT_ID, TestDataFactory.USER_ID, BigDecimal.ZERO);

        when(cryptoService.hmacHash(any())).thenReturn("HASH[xxx]");
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(saved);
        when(bankAccountMapper.toResponseDto(any())).thenReturn(buildResponseDto(saved));

        bankAccountService.linkAccount(TestDataFactory.USER_ID, request);

        var captor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getCurrency()).isEqualTo("THB");
    }

    @Test
    @DisplayName("getAccountsForUser: มีบัญชี 2 ใบ — return list 2 รายการ")
    void getAccountsForUser_returnsAll() {
        BankAccount a1 = TestDataFactory.buildActiveAccount(
                TestDataFactory.ACCOUNT_ID, TestDataFactory.USER_ID, new BigDecimal("10000"));
        BankAccount a2 = TestDataFactory.buildActiveAccount(
                TestDataFactory.ACCOUNT_ID_2, TestDataFactory.USER_ID, new BigDecimal("5000"));

        when(bankAccountRepository.findByUserId(TestDataFactory.USER_ID)).thenReturn(List.of(a1, a2));
        when(bankAccountMapper.toResponseDto(a1)).thenReturn(buildResponseDto(a1));
        when(bankAccountMapper.toResponseDto(a2)).thenReturn(buildResponseDto(a2));

        List<BankAccountResponseDto> result = bankAccountService.getAccountsForUser(TestDataFactory.USER_ID);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getAccountsForUser: ไม่มีบัญชี — return list ว่าง")
    void getAccountsForUser_empty() {
        when(bankAccountRepository.findByUserId(any())).thenReturn(List.of());
        assertThat(bankAccountService.getAccountsForUser(TestDataFactory.USER_ID)).isEmpty();
    }

    @Test
    @DisplayName("getById: พบบัญชี — return DTO")
    void getById_found() {
        BankAccount account = TestDataFactory.buildActiveAccount(
                TestDataFactory.ACCOUNT_ID, TestDataFactory.USER_ID, new BigDecimal("10000"));
        when(bankAccountRepository.findById(TestDataFactory.ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(bankAccountMapper.toResponseDto(account)).thenReturn(buildResponseDto(account));

        BankAccountResponseDto result = bankAccountService.getById(TestDataFactory.ACCOUNT_ID);
        assertThat(result.accountId()).isEqualTo(TestDataFactory.ACCOUNT_ID);
    }

    @Test
    @DisplayName("getById: ไม่พบบัญชี — throw ResourceNotFoundException")
    void getById_notFound() {
        when(bankAccountRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bankAccountService.getById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found");
    }

    private BankAccountResponseDto buildResponseDto(BankAccount account) {
        return new BankAccountResponseDto(
                account.getAccountId(), account.getUserId(), "xxxxxxx3341",
                account.getBankCode(), account.getBankName(), account.getAccountType(),
                account.getCurrency(), account.getBalance(), account.getAvailableBalance(),
                account.getStatus(), account.isPrimary(), account.getCreatedAt());
    }
}

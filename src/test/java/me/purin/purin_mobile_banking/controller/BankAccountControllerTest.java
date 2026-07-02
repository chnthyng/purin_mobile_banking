package me.purin.purin_mobile_banking.controller;

import me.purin.purin_mobile_banking.TestDataFactory;
import me.purin.purin_mobile_banking.dto.response.BankAccountResponseDto;
import me.purin.purin_mobile_banking.enums.AccountStatus;
import me.purin.purin_mobile_banking.enums.AccountType;
import me.purin.purin_mobile_banking.exception.ResourceNotFoundException;
import me.purin.purin_mobile_banking.service.BankAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BankAccountController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("BankAccountController")
class BankAccountControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean BankAccountService bankAccountService;

    private static final String BASE_URL = "/api/v1/users/{userId}/accounts";

    private BankAccountResponseDto buildDto(UUID accountId, BigDecimal balance, AccountStatus status) {
        return new BankAccountResponseDto(
                accountId,
                TestDataFactory.USER_ID,
                "xxxxxxx3341",
                "SCB",
                "Siam Commercial Bank",
                AccountType.SAVINGS,
                "THB",
                balance,
                balance,
                status,
                true,
                Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    @Nested
    @DisplayName("POST /{userId}/accounts (linkAccount)")
    class LinkAccount {

        @Test
        @DisplayName("สำเร็จ — HTTP 201 พร้อม masked account number")
        void linkAccount_success_returns201() throws Exception {
            BankAccountResponseDto dto = buildDto(TestDataFactory.ACCOUNT_ID, BigDecimal.ZERO, AccountStatus.ACTIVE);
            when(bankAccountService.linkAccount(eq(TestDataFactory.USER_ID), any())).thenReturn(dto);

            String body = """
                    {
                      "accountNumber": "0011122233341",
                      "bankCode": "SCB",
                      "bankName": "Siam Commercial Bank",
                      "accountType": "SAVINGS",
                      "currency": "THB",
                      "isPrimary": true
                    }""";

            mockMvc.perform(post(BASE_URL, TestDataFactory.USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accountId").value(TestDataFactory.ACCOUNT_ID.toString()))
                    .andExpect(jsonPath("$.maskedAccountNumber").value("xxxxxxx3341"))
                    .andExpect(jsonPath("$.bankCode").value("SCB"))
                    .andExpect(jsonPath("$.balance").value(0));
        }

        @Test
        @DisplayName("response ต้องไม่มี accountNumber หรือ accountNumberHash แบบ raw")
        void linkAccount_doesNotExposeRawAccountNumber() throws Exception {
            BankAccountResponseDto dto = buildDto(TestDataFactory.ACCOUNT_ID, BigDecimal.ZERO, AccountStatus.ACTIVE);
            when(bankAccountService.linkAccount(any(), any())).thenReturn(dto);

            String body = """
                    {
                      "accountNumber": "0011122233341",
                      "bankCode": "SCB",
                      "bankName": "Siam Commercial Bank",
                      "accountType": "SAVINGS",
                      "currency": "THB",
                      "isPrimary": true
                    }""";

            mockMvc.perform(post(BASE_URL, TestDataFactory.USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(jsonPath("$.accountNumber").doesNotExist())
                    .andExpect(jsonPath("$.accountNumberHash").doesNotExist());
        }

        @Test
        @DisplayName("accountNumber ว่าง — HTTP 400 validation error")
        void linkAccount_blankAccountNumber_returns400() throws Exception {
            String body = """
                    {
                      "accountNumber": "",
                      "bankCode": "SCB",
                      "bankName": "Siam Commercial Bank",
                      "accountType": "SAVINGS",
                      "currency": "THB",
                      "isPrimary": true
                    }""";

            mockMvc.perform(post(BASE_URL, TestDataFactory.USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.accountNumber").exists());
        }

        @Test
        @DisplayName("bankCode ว่าง — HTTP 400 validation error")
        void linkAccount_blankBankCode_returns400() throws Exception {
            String body = """
                    {
                      "accountNumber": "0011122233341",
                      "bankCode": "",
                      "bankName": "Siam Commercial Bank",
                      "accountType": "SAVINGS",
                      "currency": "THB",
                      "isPrimary": true
                    }""";

            mockMvc.perform(post(BASE_URL, TestDataFactory.USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.bankCode").exists());
        }

        @Test
        @DisplayName("accountType เป็น null — HTTP 400 validation error")
        void linkAccount_nullAccountType_returns400() throws Exception {
            String body = """
                    {
                      "accountNumber": "0011122233341",
                      "bankCode": "SCB",
                      "bankName": "Siam Commercial Bank",
                      "accountType": null,
                      "currency": "THB",
                      "isPrimary": true
                    }""";

            mockMvc.perform(post(BASE_URL, TestDataFactory.USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.accountType").exists());
        }

        @Test
        @DisplayName("currency เกิน 3 ตัวอักษร — HTTP 400 validation error")
        void linkAccount_invalidCurrencyLength_returns400() throws Exception {
            String body = """
                    {
                      "accountNumber": "0011122233341",
                      "bankCode": "SCB",
                      "bankName": "Siam Commercial Bank",
                      "accountType": "SAVINGS",
                      "currency": "BAHT",
                      "isPrimary": true
                    }""";

            mockMvc.perform(post(BASE_URL, TestDataFactory.USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.currency").exists());
        }

        @Test
        @DisplayName("body ว่างเปล่า — HTTP 400")
        void linkAccount_emptyBody_returns400() throws Exception {
            mockMvc.perform(post(BASE_URL, TestDataFactory.USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /{userId}/accounts (getAccountsForUser)")
    class GetAccountsForUser {

        @Test
        @DisplayName("มีบัญชี 2 ใบ — HTTP 200 พร้อม list 2 รายการ")
        void getAccountsForUser_returnsAll() throws Exception {
            List<BankAccountResponseDto> accounts = List.of(
                    buildDto(TestDataFactory.ACCOUNT_ID,   new BigDecimal("10000.00"), AccountStatus.ACTIVE),
                    buildDto(TestDataFactory.ACCOUNT_ID_2, new BigDecimal("5000.00"),  AccountStatus.ACTIVE)
            );
            when(bankAccountService.getAccountsForUser(TestDataFactory.USER_ID)).thenReturn(accounts);

            mockMvc.perform(get(BASE_URL, TestDataFactory.USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].accountId").value(TestDataFactory.ACCOUNT_ID.toString()))
                    .andExpect(jsonPath("$[1].accountId").value(TestDataFactory.ACCOUNT_ID_2.toString()));
        }

        @Test
        @DisplayName("ไม่มีบัญชี — HTTP 200 พร้อม list ว่าง")
        void getAccountsForUser_noAccounts_returnsEmptyList() throws Exception {
            when(bankAccountService.getAccountsForUser(any())).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL, TestDataFactory.USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("response ต้องไม่มี accountNumber หรือ accountNumberHash แบบ raw")
        void getAccountsForUser_doesNotExposeRawAccountNumber() throws Exception {
            when(bankAccountService.getAccountsForUser(any()))
                    .thenReturn(List.of(buildDto(TestDataFactory.ACCOUNT_ID, new BigDecimal("1000.00"), AccountStatus.ACTIVE)));

            mockMvc.perform(get(BASE_URL, TestDataFactory.USER_ID))
                    .andExpect(jsonPath("$[0].accountNumber").doesNotExist())
                    .andExpect(jsonPath("$[0].accountNumberHash").doesNotExist())
                    .andExpect(jsonPath("$[0].maskedAccountNumber").exists());
        }
    }

    @Nested
    @DisplayName("GET /{userId}/accounts/{accountId} (getById)")
    class GetById {

        @Test
        @DisplayName("พบบัญชี — HTTP 200 พร้อม DTO")
        void getById_found_returns200() throws Exception {
            BankAccountResponseDto dto = buildDto(TestDataFactory.ACCOUNT_ID, new BigDecimal("45230.50"), AccountStatus.ACTIVE);
            when(bankAccountService.getById(TestDataFactory.ACCOUNT_ID)).thenReturn(dto);

            mockMvc.perform(get(BASE_URL + "/{accountId}", TestDataFactory.USER_ID, TestDataFactory.ACCOUNT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountId").value(TestDataFactory.ACCOUNT_ID.toString()))
                    .andExpect(jsonPath("$.balance").value(45230.50))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("ไม่พบบัญชี — HTTP 404")
        void getById_notFound_returns404() throws Exception {
            when(bankAccountService.getById(any()))
                    .thenThrow(new ResourceNotFoundException("Account not found: " + TestDataFactory.ACCOUNT_ID));

            mockMvc.perform(get(BASE_URL + "/{accountId}", TestDataFactory.USER_ID, TestDataFactory.ACCOUNT_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Account not found: " + TestDataFactory.ACCOUNT_ID));
        }

        @Test
        @DisplayName("บัญชีสถานะ FROZEN — HTTP 200 พร้อม status FROZEN ใน response")
        void getById_frozenAccount_returns200WithFrozenStatus() throws Exception {
            BankAccountResponseDto dto = buildDto(TestDataFactory.ACCOUNT_ID, BigDecimal.ZERO, AccountStatus.FROZEN);
            when(bankAccountService.getById(TestDataFactory.ACCOUNT_ID)).thenReturn(dto);

            mockMvc.perform(get(BASE_URL + "/{accountId}", TestDataFactory.USER_ID, TestDataFactory.ACCOUNT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("FROZEN"));
        }

        @Test
        @DisplayName("accountId ไม่ใช่ UUID format — HTTP 400")
        void getById_invalidUuidFormat_returns400() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{accountId}", TestDataFactory.USER_ID, "not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }
}

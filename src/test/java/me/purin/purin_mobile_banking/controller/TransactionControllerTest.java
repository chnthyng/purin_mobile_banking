package me.purin.purin_mobile_banking.controller;

import me.purin.purin_mobile_banking.TestDataFactory;
import me.purin.purin_mobile_banking.dto.response.TransactionResponseDto;
import me.purin.purin_mobile_banking.exception.DuplicatedRequestException;
import me.purin.purin_mobile_banking.exception.InvalidTransactionException;
import me.purin.purin_mobile_banking.exception.ResourceNotFoundException;
import me.purin.purin_mobile_banking.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TransactionController")
class TransactionControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean TransactionService transactionService;

    private static final String TRANSFER_URL = "/api/v1/transactions/transfer";

    @Test
    @DisplayName("POST /transfer: สำเร็จ — HTTP 201 พร้อม transactionId")
    void transfer_success_returns201() throws Exception {
        TransactionResponseDto dto = TestDataFactory.buildTransactionResponseDto();
        when(transactionService.transfer(any(), any())).thenReturn(dto);

        String body = """
                {
                  "fromAccountId": "%s",
                  "toAccountId": "%s",
                  "transactionType": "TRANSFER",
                  "amount": 500.00,
                  "currency": "THB",
                  "description": "Test",
                  "idempotencyKey": "idem-key-test-0001"
                }""".formatted(TestDataFactory.ACCOUNT_ID, TestDataFactory.ACCOUNT_ID_2);

        mockMvc.perform(post(TRANSFER_URL)
                        .header("X-User-Id", TestDataFactory.USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(TestDataFactory.TX_ID.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("POST /transfer: ยอดไม่พอ — HTTP 422")
    void transfer_insufficientBalance_returns422() throws Exception {
        when(transactionService.transfer(any(), any()))
                .thenThrow(new InvalidTransactionException("Insufficient available balance in source account"));

        String body = """
                {
                  "fromAccountId": "%s",
                  "toAccountId": "%s",
                  "transactionType": "TRANSFER",
                  "amount": 999999.00,
                  "currency": "THB",
                  "description": "Test",
                  "idempotencyKey": "idem-key-insufficient"
                }""".formatted(TestDataFactory.ACCOUNT_ID, TestDataFactory.ACCOUNT_ID_2);

        mockMvc.perform(post(TRANSFER_URL)
                        .header("X-User-Id", TestDataFactory.USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Insufficient available balance in source account"));
    }

    @Test
    @DisplayName("POST /transfer: idempotency ซ้ำ — HTTP 200 พร้อมผลลัพธ์เดิม (ไม่ใช่ error)")
    void transfer_duplicateIdempotency_returns200WithOriginalResult() throws Exception {
        TransactionResponseDto existingDto = TestDataFactory.buildTransactionResponseDto();
        when(transactionService.transfer(any(), any()))
                .thenThrow(new DuplicatedRequestException("Duplicate request", existingDto));

        String body = """
                {
                  "fromAccountId": "%s",
                  "toAccountId": "%s",
                  "transactionType": "TRANSFER",
                  "amount": 500.00,
                  "currency": "THB",
                  "description": "Test",
                  "idempotencyKey": "idem-key-test-0001"
                }""".formatted(TestDataFactory.ACCOUNT_ID, TestDataFactory.ACCOUNT_ID_2);

        mockMvc.perform(post(TRANSFER_URL)
                        .header("X-User-Id", TestDataFactory.USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())   // 200 ไม่ใช่ error code
                .andExpect(jsonPath("$.transactionId").value(TestDataFactory.TX_ID.toString()));
    }

    @Test
    @DisplayName("POST /transfer: amount เป็น 0 — HTTP 400 validation error")
    void transfer_zeroAmount_returns400() throws Exception {
        String body = """
                {
                  "fromAccountId": "%s",
                  "toAccountId": "%s",
                  "transactionType": "TRANSFER",
                  "amount": 0,
                  "currency": "THB",
                  "idempotencyKey": "idem-key-zero"
                }""".formatted(TestDataFactory.ACCOUNT_ID, TestDataFactory.ACCOUNT_ID_2);

        mockMvc.perform(post(TRANSFER_URL)
                        .header("X-User-Id", TestDataFactory.USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }

    @Test
    @DisplayName("POST /transfer: idempotencyKey ว่าง — HTTP 400")
    void transfer_missingIdempotencyKey_returns400() throws Exception {
        String body = """
                {
                  "fromAccountId": "%s",
                  "toAccountId": "%s",
                  "transactionType": "TRANSFER",
                  "amount": 500.00,
                  "currency": "THB",
                  "idempotencyKey": ""
                }""".formatted(TestDataFactory.ACCOUNT_ID, TestDataFactory.ACCOUNT_ID_2);

        mockMvc.perform(post(TRANSFER_URL)
                        .header("X-User-Id", TestDataFactory.USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /{transactionId}: พบ — HTTP 200")
    void getById_found_returns200() throws Exception {
        TransactionResponseDto dto = TestDataFactory.buildTransactionResponseDto();
        when(transactionService.getById(TestDataFactory.TX_ID)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/transactions/{id}", TestDataFactory.TX_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(TestDataFactory.TX_ID.toString()));
    }

    @Test
    @DisplayName("GET /{transactionId}: ไม่พบ — HTTP 404")
    void getById_notFound_returns404() throws Exception {
        when(transactionService.getById(any()))
                .thenThrow(new ResourceNotFoundException("Transaction not found"));

        mockMvc.perform(get("/api/v1/transactions/{id}", TestDataFactory.TX_ID))
                .andExpect(status().isNotFound());
    }
}

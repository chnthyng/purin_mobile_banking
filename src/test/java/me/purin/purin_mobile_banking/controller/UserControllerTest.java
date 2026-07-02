package me.purin.purin_mobile_banking.controller;

import me.purin.purin_mobile_banking.TestDataFactory;
import me.purin.purin_mobile_banking.dto.response.UserResponseDto;
import me.purin.purin_mobile_banking.exception.DuplicatedResourceException;
import me.purin.purin_mobile_banking.exception.ResourceNotFoundException;
import me.purin.purin_mobile_banking.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController")
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean UserService userService;

    @Test
    @DisplayName("POST /register: สำเร็จ — HTTP 201 พร้อม userId")
    void register_success_returns201() throws Exception {
        UserResponseDto responseDto = new UserResponseDto(
                TestDataFactory.USER_ID, "somchai_p",
                "somchai.p@example.com", "+66812345671", true, Instant.now());
        when(userService.register(any())).thenReturn(responseDto);

        String body = """
                {
                  "username": "somchai_p",
                  "email": "somchai.p@example.com",
                  "phoneNumber": "+66812345671",
                  "password": "SecureP@ss123"
                }""";

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(TestDataFactory.USER_ID.toString()))
                .andExpect(jsonPath("$.username").value("somchai_p"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());  // ห้ามหลุดออกไป
    }

    @Test
    @DisplayName("POST /register: username ซ้ำ — HTTP 409")
    void register_duplicateUsername_returns409() throws Exception {
        when(userService.register(any()))
                .thenThrow(new DuplicatedResourceException("Username already taken: somchai_p"));

        String body = """
                {
                  "username": "somchai_p",
                  "email": "somchai.p@example.com",
                  "phoneNumber": "+66812345671",
                  "password": "SecureP@ss123"
                }""";

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already taken: somchai_p"));
    }

    @Test
    @DisplayName("POST /register: password สั้นเกินไป — HTTP 400 validation error")
    void register_shortPassword_returns400() throws Exception {
        String body = """
                {
                  "username": "somchai_p",
                  "email": "somchai.p@example.com",
                  "phoneNumber": "+66812345671",
                  "password": "short"
                }""";

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    @DisplayName("POST /register: email format ผิด — HTTP 400")
    void register_invalidEmail_returns400() throws Exception {
        String body = """
                {
                  "username": "somchai_p",
                  "email": "not-an-email",
                  "phoneNumber": "+66812345671",
                  "password": "SecureP@ss123"
                }""";

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    @DisplayName("POST /register: body ว่าง — HTTP 400")
    void register_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /{userId}: พบ user — HTTP 200 พร้อม DTO")
    void getById_found_returns200() throws Exception {
        UserResponseDto dto = new UserResponseDto(
                TestDataFactory.USER_ID, "somchai_p",
                "somchai.p@example.com", "+66812345671", true, Instant.now());
        when(userService.getById(TestDataFactory.USER_ID)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/{userId}", TestDataFactory.USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(TestDataFactory.USER_ID.toString()))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("GET /{userId}: ไม่พบ user — HTTP 404")
    void getById_notFound_returns404() throws Exception {
        when(userService.getById(any()))
                .thenThrow(new ResourceNotFoundException("User not found: " + TestDataFactory.USER_ID));

        mockMvc.perform(get("/api/v1/users/{userId}", TestDataFactory.USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}

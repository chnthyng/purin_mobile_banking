package me.purin.purin_mobile_banking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.purin.purin_mobile_banking.dto.request.UserRegisterRequestDto;
import me.purin.purin_mobile_banking.dto.response.UserResponseDto;
import me.purin.purin_mobile_banking.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegisterRequestDto request) {
        UserResponseDto created = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getById(userId));
    }
}
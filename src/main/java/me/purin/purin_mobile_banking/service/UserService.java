package me.purin.purin_mobile_banking.service;

import lombok.RequiredArgsConstructor;
import me.purin.purin_mobile_banking.dto.request.UserRegisterRequestDto;
import me.purin.purin_mobile_banking.dto.response.UserResponseDto;
import me.purin.purin_mobile_banking.entity.User;
import me.purin.purin_mobile_banking.entity.UserCredential;
import me.purin.purin_mobile_banking.exception.DuplicatedResourceException;
import me.purin.purin_mobile_banking.exception.ResourceNotFoundException;
import me.purin.purin_mobile_banking.mapper.UserMapper;
import me.purin.purin_mobile_banking.repository.UserCredentialRepository;
import me.purin.purin_mobile_banking.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserResponseDto register(UserRegisterRequestDto request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicatedResourceException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicatedResourceException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .isActive(true)
                .build();
        user = userRepository.save(user);

        UserCredential credential = UserCredential.builder()
                .userId(user.getUserId())
                .passwordHash(passwordEncoder.encode(request.password()))
                .algorithm("argon2id")
                .mustChangePassword(false)
                .build();
        userCredentialRepository.save(credential);

        return userMapper.toResponseDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return userMapper.toResponseDto(user);
    }
}
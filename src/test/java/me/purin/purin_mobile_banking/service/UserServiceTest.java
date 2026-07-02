package me.purin.purin_mobile_banking.service;

import me.purin.purin_mobile_banking.TestDataFactory;
import me.purin.purin_mobile_banking.dto.request.UserRegisterRequestDto;
import me.purin.purin_mobile_banking.dto.response.UserResponseDto;
import me.purin.purin_mobile_banking.entity.User;
import me.purin.purin_mobile_banking.exception.DuplicatedResourceException;
import me.purin.purin_mobile_banking.exception.ResourceNotFoundException;
import me.purin.purin_mobile_banking.mapper.UserMapper;
import me.purin.purin_mobile_banking.repository.UserCredentialRepository;
import me.purin.purin_mobile_banking.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserCredentialRepository userCredentialRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserMapper userMapper;

    @InjectMocks UserService userService;

    @Test
    @DisplayName("register: สำเร็จ — บันทึก user และ credential แยกกัน")
    void register_success() {
        UserRegisterRequestDto request = TestDataFactory.buildRegisterRequest();
        User saved = TestDataFactory.buildUser();
        UserResponseDto expectedDto = new UserResponseDto(
                saved.getUserId(), saved.getUsername(), saved.getEmail(),
                saved.getPhoneNumber(), true, saved.getCreatedAt());

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(passwordEncoder.encode(request.password())).thenReturn("HASHED_PASSWORD");
        when(userMapper.toResponseDto(saved)).thenReturn(expectedDto);

        UserResponseDto result = userService.register(request);

        assertThat(result.userId()).isEqualTo(saved.getUserId());
        assertThat(result.username()).isEqualTo("somchai_p");

        var credCaptor = ArgumentCaptor.forClass(
                me.purin.purin_mobile_banking.entity.UserCredential.class);
        verify(userCredentialRepository).save(credCaptor.capture());
        assertThat(credCaptor.getValue().getPasswordHash()).isEqualTo("HASHED_PASSWORD");
        assertThat(credCaptor.getValue().getPasswordHash()).doesNotContain(request.password());
    }

    @Test
    @DisplayName("register: username ซ้ำ — throw DuplicateResourceException")
    void register_duplicateUsername_throws() {
        UserRegisterRequestDto request = TestDataFactory.buildRegisterRequest();
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(DuplicatedResourceException.class)
                .hasMessageContaining("Username already taken");

        verify(userRepository, never()).save(any());
        verify(userCredentialRepository, never()).save(any());
    }

    @Test
    @DisplayName("register: email ซ้ำ — throw DuplicateResourceException")
    void register_duplicateEmail_throws() {
        UserRegisterRequestDto request = TestDataFactory.buildRegisterRequest();
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(DuplicatedResourceException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register: password ต้องถูก hash ก่อน save — ห้าม store plaintext")
    void register_passwordMustBeHashed() {
        UserRegisterRequestDto request = TestDataFactory.buildRegisterRequest();
        User saved = TestDataFactory.buildUser();

        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(saved);
        when(passwordEncoder.encode(request.password())).thenReturn("$argon2id$placeholder");
        when(userMapper.toResponseDto(any())).thenReturn(mock(UserResponseDto.class));

        userService.register(request);

        var captor = ArgumentCaptor.forClass(
                me.purin.purin_mobile_banking.entity.UserCredential.class);
        verify(userCredentialRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash())
                .isNotEqualTo(request.password());
    }

    @Test
    @DisplayName("getById: หา user เจอ — return DTO")
    void getById_found() {
        User user = TestDataFactory.buildUser();
        UserResponseDto dto = new UserResponseDto(
                user.getUserId(), user.getUsername(), user.getEmail(),
                user.getPhoneNumber(), true, user.getCreatedAt());

        when(userRepository.findById(TestDataFactory.USER_ID)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(dto);

        UserResponseDto result = userService.getById(TestDataFactory.USER_ID);

        assertThat(result.userId()).isEqualTo(TestDataFactory.USER_ID);
    }

    @Test
    @DisplayName("getById: ไม่พบ user — throw ResourceNotFoundException")
    void getById_notFound_throws() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(unknownId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}

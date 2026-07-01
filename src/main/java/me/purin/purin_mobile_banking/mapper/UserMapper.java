package me.purin.purin_mobile_banking.mapper;

import me.purin.purin_mobile_banking.dto.response.UserResponseDto;
import me.purin.purin_mobile_banking.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
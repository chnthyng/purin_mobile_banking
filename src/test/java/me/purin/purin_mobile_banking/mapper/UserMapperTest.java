package me.purin.purin_mobile_banking.mapper;

import me.purin.purin_mobile_banking.TestDataFactory;
import me.purin.purin_mobile_banking.dto.response.UserResponseDto;
import me.purin.purin_mobile_banking.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserMapper")
class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    @DisplayName("toResponseDto: field ทุกตัว map ตรงกับ entity")
    void toResponseDto_allFieldsMapped() {
        User user = TestDataFactory.buildUser();

        UserResponseDto dto = mapper.toResponseDto(user);

        assertThat(dto.userId()).isEqualTo(user.getUserId());
        assertThat(dto.username()).isEqualTo(user.getUsername());
        assertThat(dto.email()).isEqualTo(user.getEmail());
        assertThat(dto.phoneNumber()).isEqualTo(user.getPhoneNumber());
        assertThat(dto.isActive()).isEqualTo(user.isActive());
        assertThat(dto.createdAt()).isEqualTo(user.getCreatedAt());
    }

    @Test
    @DisplayName("toResponseDto: user ที่ inactive — isActive เป็น false")
    void toResponseDto_inactiveUser() {
        User user = TestDataFactory.buildUser();
        user.setActive(false);

        UserResponseDto dto = mapper.toResponseDto(user);

        assertThat(dto.isActive()).isFalse();
    }

    @Test
    @DisplayName("toResponseDto: phoneNumber เป็น null — dto รับค่า null ได้")
    void toResponseDto_nullPhoneNumber() {
        User user = TestDataFactory.buildUser();
        user.setPhoneNumber(null);

        UserResponseDto dto = mapper.toResponseDto(user);

        assertThat(dto.phoneNumber()).isNull();
    }

    @Test
    @DisplayName("toResponseDto: ต้องไม่มี field credential ที่ sensitive (password, failedLogin, lockedUntil)")
    void toResponseDto_doesNotExposeCredentialFields() {
        User user = TestDataFactory.buildUser();
        user.setFailedLoginCount((short) 5);

        UserResponseDto dto = mapper.toResponseDto(user);

        assertThat(dto.getClass().getDeclaredFields())
                .extracting(java.lang.reflect.Field::getName)
                .doesNotContain("passwordHash", "failedLoginCount", "lockedUntil");
    }
}
package me.purin.purin_mobile_banking.mapper;

import me.purin.purin_mobile_banking.TestDataFactory;
import me.purin.purin_mobile_banking.dto.response.BankAccountResponseDto;
import me.purin.purin_mobile_banking.entity.BankAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BankAccountMapper")
class BankAccountMapperTest {

    private final BankAccountMapper mapper = new BankAccountMapper();

    @Test
    @DisplayName("maskedAccountNumber: เปิดเผยแค่ 4 ตัวท้าย ซ่อน prefix")
    void toResponseDto_masksAccountNumber() {
        BankAccount account = TestDataFactory.buildActiveAccount(
                TestDataFactory.ACCOUNT_ID, TestDataFactory.USER_ID, new BigDecimal("10000.00"));
        account.setAccountNumber("0011122233341");  // 13 หลัก

        BankAccountResponseDto dto = mapper.toResponseDto(account);

        assertThat(dto.maskedAccountNumber()).endsWith("3341");
        assertThat(dto.maskedAccountNumber()).startsWith("x");
        assertThat(dto.maskedAccountNumber()).doesNotContain("0011122");
    }

    @Test
    @DisplayName("maskedAccountNumber: account number สั้นกว่า 4 ตัว — return xxxx")
    void toResponseDto_shortAccountNumber_returnsXxxx() {
        BankAccount account = TestDataFactory.buildActiveAccount(
                TestDataFactory.ACCOUNT_ID, TestDataFactory.USER_ID, BigDecimal.ZERO);
        account.setAccountNumber("123");

        BankAccountResponseDto dto = mapper.toResponseDto(account);
        assertThat(dto.maskedAccountNumber()).isEqualTo("xxxx");
    }

    @Test
    @DisplayName("toResponseDto: ไม่มี accountNumber และ accountNumberHash ใน response")
    void toResponseDto_doesNotExposeRawAccountNumber() {
        BankAccount account = TestDataFactory.buildActiveAccount(
                TestDataFactory.ACCOUNT_ID, TestDataFactory.USER_ID, new BigDecimal("10000.00"));
        account.setAccountNumber("0011122233341");
        account.setAccountNumberHash("HASH[0011122233341]");

        BankAccountResponseDto dto = mapper.toResponseDto(account);

        assertThat(dto.maskedAccountNumber()).doesNotContain("HASH");
        assertThat(dto.maskedAccountNumber()).isNotEqualTo(account.getAccountNumber());
    }

    @Test
    @DisplayName("toResponseDto: field อื่นๆ map ตรงกับ entity")
    void toResponseDto_fieldsMappedCorrectly() {
        BankAccount account = TestDataFactory.buildActiveAccount(
                TestDataFactory.ACCOUNT_ID, TestDataFactory.USER_ID, new BigDecimal("45230.50"));

        BankAccountResponseDto dto = mapper.toResponseDto(account);

        assertThat(dto.accountId()).isEqualTo(account.getAccountId());
        assertThat(dto.userId()).isEqualTo(account.getUserId());
        assertThat(dto.bankCode()).isEqualTo("SCB");
        assertThat(dto.balance()).isEqualByComparingTo(new BigDecimal("45230.50"));
        assertThat(dto.status()).isEqualTo(account.getStatus());
        assertThat(dto.isPrimary()).isTrue();
    }
}

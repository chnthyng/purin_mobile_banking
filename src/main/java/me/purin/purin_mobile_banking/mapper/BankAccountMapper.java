package me.purin.purin_mobile_banking.mapper;

import me.purin.purin_mobile_banking.dto.response.BankAccountResponseDto;
import me.purin.purin_mobile_banking.entity.BankAccount;
import org.springframework.stereotype.Component;

@Component
public class BankAccountMapper {

    public BankAccountResponseDto toResponseDto(BankAccount account) {
        return new BankAccountResponseDto(
                account.getAccountId(),
                account.getUserId(),
                maskAccountNumber(account.getAccountNumber()),
                account.getBankCode(),
                account.getBankName(),
                account.getAccountType(),
                account.getCurrency(),
                account.getBalance(),
                account.getAvailableBalance(),
                account.getStatus(),
                account.isPrimary(),
                account.getCreatedAt()
        );
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return "xxxx";
        }
        int visibleLength = 4;
        String masked = "x".repeat(accountNumber.length() - visibleLength);
        return masked + accountNumber.substring(accountNumber.length() - visibleLength);
    }
}

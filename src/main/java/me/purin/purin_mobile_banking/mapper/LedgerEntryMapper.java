package me.purin.purin_mobile_banking.mapper;

import me.purin.purin_mobile_banking.dto.response.LedgerEntryResponseDto;
import me.purin.purin_mobile_banking.entity.LedgerEntry;
import org.springframework.stereotype.Component;

@Component
public class LedgerEntryMapper {

    public LedgerEntryResponseDto toResponseDto(LedgerEntry entry) {
        return new LedgerEntryResponseDto(
                entry.getEntryId(),
                entry.getTransactionId(),
                entry.getAccountId(),
                entry.getEntryType(),
                entry.getAmount(),
                entry.getCreatedAt()
        );
    }
}
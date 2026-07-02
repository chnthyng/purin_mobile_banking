package me.purin.purin_mobile_banking.mapper;

import me.purin.purin_mobile_banking.TestDataFactory;
import me.purin.purin_mobile_banking.dto.response.LedgerEntryResponseDto;
import me.purin.purin_mobile_banking.entity.LedgerEntry;
import me.purin.purin_mobile_banking.enums.EntryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LedgerEntryMapper")
class LedgerEntryMapperTest {

    private final LedgerEntryMapper mapper = new LedgerEntryMapper();

    private LedgerEntry buildEntry(EntryType entryType, BigDecimal amount) {
        LedgerEntry entry = new LedgerEntry();
        entry.setEntryId(UUID.fromString("cccc0001-cccc-0001-cccc-000000000001"));
        entry.setTransactionId(TestDataFactory.TX_ID);
        entry.setAccountId(TestDataFactory.ACCOUNT_ID);
        entry.setEntryType(entryType);
        entry.setAmount(amount);
        entry.setCreatedAt(Instant.parse("2024-06-01T10:00:00Z"));
        return entry;
    }

    @Test
    @DisplayName("toResponseDto: DEBIT entry — field ทุกตัว map ตรงกับ entity")
    void toResponseDto_debitEntry_allFieldsMapped() {
        LedgerEntry entry = buildEntry(EntryType.DEBIT, new BigDecimal("1500.00"));

        LedgerEntryResponseDto dto = mapper.toResponseDto(entry);

        assertThat(dto.entryId()).isEqualTo(entry.getEntryId());
        assertThat(dto.transactionId()).isEqualTo(TestDataFactory.TX_ID);
        assertThat(dto.accountId()).isEqualTo(TestDataFactory.ACCOUNT_ID);
        assertThat(dto.entryType()).isEqualTo(EntryType.DEBIT);
        assertThat(dto.amount()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(dto.createdAt()).isEqualTo(entry.getCreatedAt());
    }

    @Test
    @DisplayName("toResponseDto: CREDIT entry — entryType เป็น CREDIT")
    void toResponseDto_creditEntry() {
        LedgerEntry entry = buildEntry(EntryType.CREDIT, new BigDecimal("1500.00"));

        LedgerEntryResponseDto dto = mapper.toResponseDto(entry);

        assertThat(dto.entryType()).isEqualTo(EntryType.CREDIT);
    }

    @Test
    @DisplayName("toResponseDto: double-entry invariant — DEBIT amount == CREDIT amount")
    void toResponseDto_doubleEntryAmountConsistency() {
        BigDecimal transferAmount = new BigDecimal("750.00");
        LedgerEntry debitEntry  = buildEntry(EntryType.DEBIT,  transferAmount);
        LedgerEntry creditEntry = buildEntry(EntryType.CREDIT, transferAmount);

        LedgerEntryResponseDto debitDto  = mapper.toResponseDto(debitEntry);
        LedgerEntryResponseDto creditDto = mapper.toResponseDto(creditEntry);

        assertThat(debitDto.amount()).isEqualByComparingTo(creditDto.amount());
    }

    @Test
    @DisplayName("toResponseDto: amount มีทศนิยม 2 ตำแหน่ง — precision ถูกต้อง")
    void toResponseDto_decimalPrecision() {
        LedgerEntry entry = buildEntry(EntryType.DEBIT, new BigDecimal("100.50"));

        LedgerEntryResponseDto dto = mapper.toResponseDto(entry);

        assertThat(dto.amount()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(dto.amount().toPlainString()).isEqualTo("100.50");
    }
}

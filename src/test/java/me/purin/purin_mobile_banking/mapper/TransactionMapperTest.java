package me.purin.purin_mobile_banking.mapper;

import me.purin.purin_mobile_banking.TestDataFactory;
import me.purin.purin_mobile_banking.dto.response.TransactionResponseDto;
import me.purin.purin_mobile_banking.entity.Transaction;
import me.purin.purin_mobile_banking.enums.TransactionStatus;
import me.purin.purin_mobile_banking.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionMapper")
class TransactionMapperTest {

    private final TransactionMapper mapper = new TransactionMapper();

    @Test
    @DisplayName("toResponseDto: field ทุกตัว map ตรงกับ entity")
    void toResponseDto_allFieldsMapped() {
        Transaction tx = TestDataFactory.buildCompletedTransaction();

        TransactionResponseDto dto = mapper.toResponseDto(tx);

        assertThat(dto.transactionId()).isEqualTo(tx.getTransactionId());
        assertThat(dto.referenceNo()).isEqualTo(tx.getReferenceNo());
        assertThat(dto.fromAccountId()).isEqualTo(tx.getFromAccountId());
        assertThat(dto.toAccountId()).isEqualTo(tx.getToAccountId());
        assertThat(dto.transactionType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(dto.amount()).isEqualByComparingTo(tx.getAmount());
        assertThat(dto.currency()).isEqualTo("THB");
        assertThat(dto.fee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.status()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(dto.balanceBefore()).isEqualByComparingTo(tx.getBalanceBefore());
        assertThat(dto.balanceAfter()).isEqualByComparingTo(tx.getBalanceAfter());
        assertThat(dto.initiatedBy()).isEqualTo(tx.getInitiatedBy());
        assertThat(dto.createdAt()).isEqualTo(tx.getCreatedAt());
        assertThat(dto.completedAt()).isEqualTo(tx.getCompletedAt());
    }

    @Test
    @DisplayName("toResponseDto: deposit — fromAccountId เป็น null ได้")
    void toResponseDto_nullFromAccount() {
        Transaction tx = TestDataFactory.buildCompletedTransaction();
        tx.setFromAccountId(null);
        tx.setTransactionType(TransactionType.DEPOSIT);

        TransactionResponseDto dto = mapper.toResponseDto(tx);

        assertThat(dto.fromAccountId()).isNull();
        assertThat(dto.transactionType()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    @DisplayName("toResponseDto: withdrawal — toAccountId เป็น null ได้")
    void toResponseDto_nullToAccount() {
        Transaction tx = TestDataFactory.buildCompletedTransaction();
        tx.setToAccountId(null);
        tx.setTransactionType(TransactionType.WITHDRAWAL);

        TransactionResponseDto dto = mapper.toResponseDto(tx);

        assertThat(dto.toAccountId()).isNull();
        assertThat(dto.transactionType()).isEqualTo(TransactionType.WITHDRAWAL);
    }

    @Test
    @DisplayName("toResponseDto: pending transaction — completedAt เป็น null")
    void toResponseDto_pendingTransaction_nullCompletedAt() {
        Transaction tx = TestDataFactory.buildCompletedTransaction();
        tx.setStatus(TransactionStatus.PENDING);
        tx.setCompletedAt(null);
        tx.setBalanceAfter(null);

        TransactionResponseDto dto = mapper.toResponseDto(tx);

        assertThat(dto.status()).isEqualTo(TransactionStatus.PENDING);
        assertThat(dto.completedAt()).isNull();
        assertThat(dto.balanceAfter()).isNull();
    }

    @Test
    @DisplayName("toResponseDto: metadata JSONB ถูก map ผ่านเป็น Map ได้")
    void toResponseDto_metadataMapped() {
        Transaction tx = TestDataFactory.buildCompletedTransaction();
        tx.setMetadata(Map.of("channel", "mobile_app", "merchant_id", "MERCH-001"));

        TransactionResponseDto dto = mapper.toResponseDto(tx);

        assertThat(dto.metadata()).containsKey("channel");
        assertThat(dto.metadata().get("channel")).isEqualTo("mobile_app");
    }

    @Test
    @DisplayName("toResponseDto: metadata เป็น null — dto รับค่า null ได้")
    void toResponseDto_nullMetadata() {
        Transaction tx = TestDataFactory.buildCompletedTransaction();
        tx.setMetadata(null);

        TransactionResponseDto dto = mapper.toResponseDto(tx);

        assertThat(dto.metadata()).isNull();
    }
}

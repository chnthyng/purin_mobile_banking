package me.purin.purin_mobile_banking.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CryptoService")
class CryptoServiceTest {

    private static final String TEST_KEY = "test-hmac-secret-key-32-bytes!!";

    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = new CryptoService(TEST_KEY);
    }

    @Nested
    @DisplayName("ความถูกต้องของ HMAC output")
    class Correctness {

        @Test
        @DisplayName("hmacHash: คืนค่าที่ไม่ใช่ null และไม่ว่าง")
        void hmacHash_returnsNonNullNonEmpty() {
            String result = cryptoService.hmacHash("0011122233341");

            assertThat(result).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("hmacHash: output เป็น Base64 string ที่ valid")
        void hmacHash_outputIsBase64() {
            String result = cryptoService.hmacHash("0011122233341");

            assertThat(result).matches("^[A-Za-z0-9+/=]+$");
        }

        @Test
        @DisplayName("hmacHash: HMAC-SHA256 output ต้องมีความยาว 44 ตัวอักษร (32 bytes base64)")
        void hmacHash_outputLength() {
            String result = cryptoService.hmacHash("any-input");

            assertThat(result).hasSize(44);
        }
    }

    @Nested
    @DisplayName("Determinism — input เดิม ต้อง hash ออกมาเหมือนกันเสมอ")
    class Determinism {

        @Test
        @DisplayName("เรียก 2 ครั้งด้วย input เดิม — output เหมือนกัน")
        void hmacHash_sameInput_sameOutput() {
            String input = "0011122233341";

            String first  = cryptoService.hmacHash(input);
            String second = cryptoService.hmacHash(input);

            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("สร้าง CryptoService ใหม่ด้วย key เดิม — hash ยังคงเหมือนกัน")
        void hmacHash_sameKeyNewInstance_sameOutput() {
            CryptoService another = new CryptoService(TEST_KEY);

            String hash1 = cryptoService.hmacHash("0011122233341");
            String hash2 = another.hmacHash("0011122233341");

            assertThat(hash1).isEqualTo(hash2);
        }
    }

    @Nested
    @DisplayName("Sensitivity — input ต่างกัน ต้อง hash ต่างกัน")
    class Sensitivity {

        @Test
        @DisplayName("input ต่างกัน — output ต่างกัน")
        void hmacHash_differentInputs_differentOutputs() {
            String hash1 = cryptoService.hmacHash("0011122233341");
            String hash2 = cryptoService.hmacHash("0011122233342");  // เลขท้ายต่างกัน 1 ตัว

            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("key ต่างกัน — output ต่างกัน (กัน cross-environment collision)")
        void hmacHash_differentKeys_differentOutputs() {
            CryptoService anotherKeyService = new CryptoService("different-key-32-bytes-padding!!");

            String hash1 = cryptoService.hmacHash("0011122233341");
            String hash2 = anotherKeyService.hmacHash("0011122233341");

            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("hash ต้องไม่เปิดเผย plaintext ใน output — output ไม่มีส่วนของ input อยู่")
        void hmacHash_outputDoesNotContainPlaintext() {
            String accountNumber = "0011122233341";
            String result = cryptoService.hmacHash(accountNumber);

            assertThat(result).doesNotContain(accountNumber);
        }

        @Test
        @DisplayName("ตัวพิมพ์ใหญ่เล็กต่างกัน — hash ต่างกัน (case-sensitive)")
        void hmacHash_caseSensitive() {
            String hash1 = cryptoService.hmacHash("SCB0011122233341");
            String hash2 = cryptoService.hmacHash("scb0011122233341");

            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("whitespace ต่างกัน — hash ต่างกัน")
        void hmacHash_whitespaceSensitive() {
            String hash1 = cryptoService.hmacHash("0011122233341");
            String hash2 = cryptoService.hmacHash(" 0011122233341");

            assertThat(hash1).isNotEqualTo(hash2);
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("empty string — ยังคืน hash ได้ (ไม่ throw)")
        void hmacHash_emptyString_returnsHash() {
            String result = cryptoService.hmacHash("");

            assertThat(result).isNotNull().isNotEmpty().hasSize(44);
        }

        @Test
        @DisplayName("Unicode / Thai characters — hash ได้ถูกต้อง")
        void hmacHash_unicodeInput() {
            String result = cryptoService.hmacHash("บัญชีออมทรัพย์");

            assertThat(result).isNotNull().hasSize(44);
        }

        @Test
        @DisplayName("input ยาวมาก — hash length ยังคง 44 ตัวเสมอ (HMAC-SHA256 fixed-length output)")
        void hmacHash_longInput_fixedOutputLength() {
            String longInput = "0".repeat(1000);
            String result = cryptoService.hmacHash(longInput);

            assertThat(result).hasSize(44);
        }
    }
}
package org.example;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private FeeService feeService;
    @Mock
    private AuditLog auditLog;
    @InjectMocks
    private TransferService transferService;

    @Test
    void testSuccessfulTransfer() {
        Account from = new Account("1", new BigDecimal("1000"));
        Account to = new Account("2", new BigDecimal("500"));
        
        when(accountRepository.findById("1")).thenReturn(from);
        when(accountRepository.findById("2")).thenReturn(to);
        when(feeService.calculateFee(new BigDecimal("100"))).thenReturn(new BigDecimal("5"));

        transferService.transfer("1", "2", new BigDecimal("100"));

        verify(accountRepository, times(1)).save(from);
        verify(accountRepository, times(1)).save(to);
        verify(auditLog, times(1)).logTransfer("1", "2", new BigDecimal("100"));

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(from.getBalance()).as("From account balance should be reduced").isEqualTo(new BigDecimal("895"));
        softly.assertThat(to.getBalance()).as("To account balance should be increased").isEqualTo(new BigDecimal("600"));
        softly.assertAll();
    }

    @Test
    void testTransferAccountNotFound() {
        when(accountRepository.findById("1")).thenReturn(null);

        assertThatThrownBy(() -> transferService.transfer("1", "2", new BigDecimal("100")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Account not found");

        verify(accountRepository, never()).save(any(Account.class));
        verify(auditLog, never()).logTransfer(anyString(), anyString(), any(BigDecimal.class));
    }

    @Test
    void testTransferInsufficientFunds() {
        Account from = new Account("1", new BigDecimal("50")); // Тільки 50 на рахунку
        Account to = new Account("2", new BigDecimal("500"));

        when(accountRepository.findById("1")).thenReturn(from);
        when(accountRepository.findById("2")).thenReturn(to);
        when(feeService.calculateFee(new BigDecimal("100"))).thenReturn(new BigDecimal("5"));

        assertThatThrownBy(() -> transferService.transfer("1", "2", new BigDecimal("100")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Insufficient funds");

        verify(accountRepository, never()).save(any(Account.class));
        verify(auditLog, never()).logTransfer(anyString(), anyString(), any(BigDecimal.class));
    }

    @Test
    void testTransferZeroAmount() {
        assertThatThrownBy(() -> transferService.transfer("1", "2", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transfer amount must be positive");
    }

    @Test
    void testTransferExactFunds() {
        Account from = new Account("1", new BigDecimal("105")); // 100 + 5
        Account to = new Account("2", new BigDecimal("500"));

        when(accountRepository.findById("1")).thenReturn(from);
        when(accountRepository.findById("2")).thenReturn(to);
        when(feeService.calculateFee(new BigDecimal("100"))).thenReturn(new BigDecimal("5"));

        transferService.transfer("1", "2", new BigDecimal("100"));

        assertThat(from.getBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(to.getBalance()).isEqualTo(new BigDecimal("600"));
    }

    @Test
    void testGetRecentTransactions() {
        List<String> mockTransactions = Arrays.asList("tx1_amount_100", "tx2_amount_50", "tx3_amount_200");
        when(auditLog.getTransactions("1")).thenReturn(mockTransactions);

        List<String> transactions = transferService.getRecentTransactions("1");

        assertThat(transactions).hasSize(3).contains("tx2_amount_50").doesNotContain("tx4_amount_500")
                .containsExactly("tx1_amount_100", "tx2_amount_50", "tx3_amount_200").anyMatch(tx -> tx.startsWith("tx3"));
    }
}
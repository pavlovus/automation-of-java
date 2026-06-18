package org.example;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {
    @Test
    void testAccountGettersAndSetters() {
        Account account = new Account("123", new BigDecimal("100"));
        
        assertThat(account.getId()).isEqualTo("123");
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("100"));
        
        account.setId("456");
        account.setBalance(new BigDecimal("200"));
        
        assertThat(account.getId()).isEqualTo("456");
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("200"));
    }
}
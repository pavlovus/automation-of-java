package org.example;

import java.math.BigDecimal;
import java.util.List;

public interface AuditLog {
    void logTransfer(String fromId, String toId, BigDecimal amount);
    List<String> getTransactions(String accountId);
}
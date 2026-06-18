package org.example;

import java.math.BigDecimal;
import java.util.List;

public class TransferService {
    private final AccountRepository accountRepository;
    private final FeeService feeService;
    private final AuditLog auditLog;

    public TransferService(AccountRepository accountRepository, FeeService feeService, AuditLog auditLog) {
        this.accountRepository = accountRepository;
        this.feeService = feeService;
        this.auditLog = auditLog;
    }

    public void transfer(String fromId, String toId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Transfer amount must be positive");

        Account from = accountRepository.findById(fromId);
        Account to = accountRepository.findById(toId);

        if (from == null || to == null) throw new IllegalArgumentException("Account not found");

        BigDecimal fee = feeService.calculateFee(amount);
        BigDecimal totalDeduction = amount.add(fee);

        if (from.getBalance().compareTo(totalDeduction) < 0) throw new IllegalStateException("Insufficient funds");

        from.setBalance(from.getBalance().subtract(totalDeduction));
        to.setBalance(to.getBalance().add(amount));

        accountRepository.save(from);
        accountRepository.save(to);
        auditLog.logTransfer(fromId, toId, amount);
    }

    public List<String> getRecentTransactions(String accountId) {
        return auditLog.getTransactions(accountId);
    }
}
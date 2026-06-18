package org.example;

public interface AccountRepository {
    Account findById(String id);
    void save(Account account);
}
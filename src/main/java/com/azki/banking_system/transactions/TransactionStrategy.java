package com.azki.banking_system.transactions;

import com.azki.banking_system.entities.BankAccountEntity;

public interface TransactionStrategy {
    void processTransaction(BankAccountEntity account, double amount);
}

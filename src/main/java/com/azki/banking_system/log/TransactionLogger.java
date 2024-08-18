package com.azki.banking_system.log;

public interface TransactionLogger {
    void onTransaction(String accountNumber, String transactionType, double amount);
}

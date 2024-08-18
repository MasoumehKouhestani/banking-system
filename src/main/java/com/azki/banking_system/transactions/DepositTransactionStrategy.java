package com.azki.banking_system.transactions;

import com.azki.banking_system.entities.BankAccountEntity;

public class DepositTransactionStrategy implements TransactionStrategy {
    @Override
    public void processTransaction(BankAccountEntity account, double amount) {
        account.deposit(amount);
    }
}

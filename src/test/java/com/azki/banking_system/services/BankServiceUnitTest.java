package com.azki.banking_system.services;

import com.azki.banking_system.entities.BankAccountEntity;
import com.azki.banking_system.log.TransactionFileLogger;
import com.azki.banking_system.repositories.BankAccountRepository;
import com.azki.banking_system.transactions.DepositTransactionStrategy;
import com.azki.banking_system.transactions.WithdrawTransactionStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@ExtendWith(MockitoExtension.class)
public class BankServiceUnitTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionFileLogger transactionFileLogger;

    private BankService service;

    @BeforeEach
    public void setUp() {
        int threadPoolCapacity = 5;
        service = new BankService(bankAccountRepository, threadPoolCapacity, transactionFileLogger);
    }

    @Test
    void testDeposit() throws ExecutionException, InterruptedException {
        double balance = 1000;
        var account = new BankAccountEntity(UUID.randomUUID().toString(), balance, "Test", "TestBank");
        Mockito.when(bankAccountRepository.findByAccountNumber(account.getAccountNumber())).thenReturn(Optional.of(account));

        double amount = 5000;
        service.deposit(account.getAccountNumber(), amount, new DepositTransactionStrategy());

        Assertions.assertEquals(balance + amount, account.getBalance());
    }

    @Test
    void testWithdraw() throws ExecutionException, InterruptedException {
        double balance = 5000;
        var account = new BankAccountEntity(UUID.randomUUID().toString(), balance, "Test", "TestBank");
        Mockito.when(bankAccountRepository.findByAccountNumber(account.getAccountNumber())).thenReturn(Optional.of(account));

        double amount = 1000;
        service.withdraw(account.getAccountNumber(), amount, new WithdrawTransactionStrategy());

        Assertions.assertEquals(balance - amount, account.getBalance());
    }

    @Test
    void testTransfer() throws ExecutionException, InterruptedException {
        double balance = 5000;
        var origin = new BankAccountEntity(UUID.randomUUID().toString(), balance, "origin", "TestBank");
        Mockito.when(bankAccountRepository.findByAccountNumber(origin.getAccountNumber())).thenReturn(Optional.of(origin));

        var dest = new BankAccountEntity(UUID.randomUUID().toString(), balance, "dest", "TestBank");
        Mockito.when(bankAccountRepository.findByAccountNumber(dest.getAccountNumber())).thenReturn(Optional.of(dest));

        double amount = 1000;
        service.transferFund(origin.getAccountNumber(), dest.getAccountNumber(), amount);

        Assertions.assertEquals(balance - amount, origin.getBalance());
        Assertions.assertEquals(balance + amount, dest.getBalance());
    }

}

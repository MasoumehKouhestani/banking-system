package com.azki.banking_system.services;

import com.azki.banking_system.dto.BankAccountDto;
import com.azki.banking_system.entities.BankAccountEntity;
import com.azki.banking_system.exceptions.AccountNotFoundException;
import com.azki.banking_system.log.LogModel;
import com.azki.banking_system.log.TransactionFileLogger;
import com.azki.banking_system.log.TransactionLogSubject;
import com.azki.banking_system.repositories.BankAccountRepository;
import com.azki.banking_system.transactions.TransactionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import static com.azki.banking_system.utils.Constants.*;

@Service
public class BankService extends TransactionLogSubject {

    private final BankAccountRepository bankAccountRepository;
    private final ReentrantLock lock = new ReentrantLock();
    private final ExecutorService executorService;

    @Autowired
    public BankService(BankAccountRepository bankAccountRepository,
                       @Value("${thread.pool.capacity}") int threadPoolCapacity,
                       TransactionFileLogger logger) {
        this.bankAccountRepository = bankAccountRepository;
        executorService = Executors.newFixedThreadPool(threadPoolCapacity);
        addObserver(logger);
    }

    @Transactional
    public BankAccountDto createAccount(BankAccountDto account) {
        String accountNumber = UUID.randomUUID().toString();
        var accountEntity = new BankAccountEntity(
                accountNumber,
                account.balance(),
                account.accountHolderName(),
                account.bankName());
        return toAccountDto(bankAccountRepository.save(accountEntity));
    }

    @Transactional
    public void deleteAccountByNumber(String accountNumber) {
        bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_NOT_FOUND_ERROR_MESSAGE));

        bankAccountRepository.deleteByAccountNumber(accountNumber);
    }

    @Transactional(readOnly = true)
    public BankAccountDto getAccountByNumber(String accountNumber) {
        Optional<BankAccountEntity> byAccountNumber = bankAccountRepository.findByAccountNumber(accountNumber);
        var accountEntity = byAccountNumber
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_NOT_FOUND_ERROR_MESSAGE));

        return toAccountDto(accountEntity);
    }

    @Transactional
    public BankAccountDto deposit(String accountNumber, double amount, TransactionStrategy strategy) throws ExecutionException, InterruptedException {
        Callable<BankAccountDto> task = () -> {
            BankAccountEntity accountEntity;

            lock.lock();
            try {
                accountEntity = bankAccountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_NOT_FOUND_ERROR_MESSAGE));

                strategy.processTransaction(accountEntity, amount);
                bankAccountRepository.save(accountEntity);
            } finally {
                lock.unlock();
            }

            notifyObservers(new LogModel(accountNumber, DEPOSIT, amount));
            return toAccountDto(accountEntity);
        };

        return executorService.submit(task).get();
    }

    @Transactional
    public BankAccountDto withdraw(String accountNumber, double amount, TransactionStrategy strategy) throws ExecutionException, InterruptedException {
        Callable<BankAccountDto> task = () -> {
            BankAccountEntity accountEntity;

            lock.lock();
            try {
                accountEntity = bankAccountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_NOT_FOUND_ERROR_MESSAGE));

                strategy.processTransaction(accountEntity, amount);
                bankAccountRepository.save(accountEntity);
            } finally {
                lock.unlock();
            }

            notifyObservers(new LogModel(accountNumber, WITHDRAW, amount));
            return toAccountDto(accountEntity);
        };

        return executorService.submit(task).get();
    }

    @Transactional
    public List<BankAccountDto> transferFund(String origin, String destination, double amount) throws ExecutionException, InterruptedException {
        Callable<List<BankAccountDto>> task = () -> {

            BankAccountEntity originEntity;
            BankAccountEntity destEntity;

            lock.lock();
            try {
                originEntity = bankAccountRepository.findByAccountNumber(origin)
                        .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_NOT_FOUND_ERROR_MESSAGE));

                destEntity = bankAccountRepository.findByAccountNumber(destination)
                        .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_NOT_FOUND_ERROR_MESSAGE));

                originEntity.withdraw(amount);
                destEntity.deposit(amount);

                bankAccountRepository.save(originEntity);
                bankAccountRepository.save(destEntity);

            } finally {
                lock.unlock();
            }

            notifyObservers(new LogModel(origin, WITHDRAW, amount));
            notifyObservers(new LogModel(destination, DEPOSIT, amount));

            return List.of(toAccountDto(originEntity), toAccountDto(destEntity));
        };

        return executorService.submit(task).get();
    }

    @Transactional(readOnly = true)
    public double getBalance(String accountNumber) {
        var accountEntity = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_NOT_FOUND_ERROR_MESSAGE));
        return accountEntity.getBalance();
    }

    private BankAccountDto toAccountDto(BankAccountEntity accountEntity) {
        return new BankAccountDto(accountEntity.getId(),
                accountEntity.getAccountNumber(),
                accountEntity.getBalance(),
                accountEntity.getAccountHolderName(),
                accountEntity.getBankName());
    }
}

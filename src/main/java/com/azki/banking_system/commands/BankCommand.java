package com.azki.banking_system.commands;

import com.azki.banking_system.dto.BankAccountDto;
import com.azki.banking_system.exceptions.AccountNotFoundException;
import com.azki.banking_system.exceptions.NotEnoughBalanceException;
import com.azki.banking_system.services.BankService;
import com.azki.banking_system.transactions.DepositTransactionStrategy;
import com.azki.banking_system.transactions.WithdrawTransactionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.concurrent.ExecutionException;

import static com.azki.banking_system.utils.Constants.*;

@ShellComponent
public class BankCommand {

    private final BankService bankService;

    @Autowired
    public BankCommand(BankService bankService) {
        this.bankService = bankService;
    }

    @ShellMethod(key = "create_account")
    public ResponseEntity<BankAccountResponse> create(
            @ShellOption(value = "balance", defaultValue = "1000.0") double balance,
            @ShellOption(value = "accountHolderName", defaultValue = "unknown") String accountHolderName,
            @ShellOption(value = "bankName", defaultValue = "unknown") String bankName
    ) {
        var account = new BankAccountDto(null,
                null,
                balance,
                accountHolderName,
                bankName);
        var result = bankService.createAccount(account);
        if (result.accountNumber().isEmpty()) {
            return new ResponseEntity<>(new BankAccountResponse(null, SOMETHING_WENT_WRONG),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(new BankAccountResponse(result, SUCCESS), HttpStatus.CREATED);
    }

    @ShellMethod(key = "delete_account")
    public ResponseEntity<String> deleteByAccountNumber(
            @ShellOption("account_number") String accountNumber) {
        try {
            bankService.deleteAccountByNumber(accountNumber);
            return new ResponseEntity<>(SUCCESS, HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return new ResponseEntity<>(ACCOUNT_NOT_FOUND_ERROR_MESSAGE, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ShellMethod(key = "get_account")
    public ResponseEntity<BankAccountResponse> getByAccountNumber(
            @ShellOption("account_number") String accountNumber) {
        try {
            var account = bankService.getAccountByNumber(accountNumber);
            return new ResponseEntity<>(new BankAccountResponse(account, SUCCESS), HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return new ResponseEntity<>(new BankAccountResponse(null, ACCOUNT_NOT_FOUND_ERROR_MESSAGE), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new BankAccountResponse(null, SOMETHING_WENT_WRONG), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ShellMethod(key = "deposit")
    public ResponseEntity<BankAccountResponse> deposit(
            @ShellOption("account_number") String accountNumber,
            @ShellOption("amount") double amount) {
        try {
            var result = bankService.deposit(accountNumber, amount, new DepositTransactionStrategy());
            return new ResponseEntity<>(new BankAccountResponse(result, SUCCESS), HttpStatus.OK);
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof AccountNotFoundException) {
                return new ResponseEntity<>(new BankAccountResponse(null, ACCOUNT_NOT_FOUND_ERROR_MESSAGE), HttpStatus.NOT_FOUND);
            }
            e.printStackTrace();
            return new ResponseEntity<>(new BankAccountResponse(null, SOMETHING_WENT_WRONG), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ShellMethod(key = "withdraw")
    public ResponseEntity<BankAccountResponse> withdraw(
            @ShellOption("account_number") String accountNumber,
            @ShellOption("amount") double amount) {
        try {
            var result = bankService.withdraw(accountNumber, amount, new WithdrawTransactionStrategy());
            return new ResponseEntity<>(new BankAccountResponse(result, SUCCESS), HttpStatus.OK);
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof AccountNotFoundException) {
                return new ResponseEntity<>(new BankAccountResponse(null, ACCOUNT_NOT_FOUND_ERROR_MESSAGE), HttpStatus.NOT_FOUND);
            } else if (e.getCause() instanceof NotEnoughBalanceException) {
                return new ResponseEntity<>(new BankAccountResponse(null, NOT_ENOUGH_BALANCE_ERROR_MESSAGE), HttpStatus.BAD_REQUEST);
            }
            e.printStackTrace();
            return new ResponseEntity<>(new BankAccountResponse(null, SOMETHING_WENT_WRONG), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ShellMethod(key = "transfer")
    public ResponseEntity<TransferResponse> transferTo(
            @ShellOption("origin") String origin,
            @ShellOption("dest") String dest,
            @ShellOption("amount") double amount
    ) {
        try {
            var result = bankService.transferFund(origin, dest, amount);
            return new ResponseEntity<>(new TransferResponse(result, SUCCESS), HttpStatus.OK);
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof AccountNotFoundException) {
                e.printStackTrace();
                return new ResponseEntity<>(new TransferResponse(null, ACCOUNT_NOT_FOUND_ERROR_MESSAGE), HttpStatus.NOT_FOUND);
            } else if (e.getCause() instanceof NotEnoughBalanceException) {
                return new ResponseEntity<>(new TransferResponse(null, NOT_ENOUGH_BALANCE_ERROR_MESSAGE), HttpStatus.BAD_REQUEST);
            }
            e.printStackTrace();
            return new ResponseEntity<>(new TransferResponse(null, SOMETHING_WENT_WRONG), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ShellMethod(key = "get_balance")
    public ResponseEntity<GetBalanceResponse> getBalance(@ShellOption("account_number") String accountNumber) {
        try {
            double balance = bankService.getBalance(accountNumber);
            return new ResponseEntity<>(new GetBalanceResponse(balance, SUCCESS), HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return new ResponseEntity<>(new GetBalanceResponse(null, ACCOUNT_NOT_FOUND_ERROR_MESSAGE), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new GetBalanceResponse(null, SOMETHING_WENT_WRONG), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

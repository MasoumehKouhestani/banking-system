package com.azki.banking_system.controllers;

import com.azki.banking_system.dto.BankAccountDto;
import com.azki.banking_system.exceptions.AccountNotFoundException;
import com.azki.banking_system.exceptions.NotEnoughBalanceException;
import com.azki.banking_system.services.BankService;
import com.azki.banking_system.transactions.DepositTransactionStrategy;
import com.azki.banking_system.transactions.WithdrawTransactionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/bank")
public class BankController {

    private final BankService bankService;

    @Autowired
    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping("/account")
    public ResponseEntity<BankAccountDto> create(@RequestBody BankAccountDto account) {
        var result = bankService.createAccount(account);
        if (result.accountNumber().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @DeleteMapping("/account/{account_number}")
    public ResponseEntity<Void> deleteByAccountNumber(@PathVariable("account_number") String accountNumber) {
        try {
            bankService.deleteAccountByNumber(accountNumber);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/account/{account_number}", produces = "application/json")
    public ResponseEntity<BankAccountDto> getByAccountNumber(@PathVariable("account_number") String accountNumber) {
        try {
            var account = bankService.getAccountByNumber(accountNumber);
            return new ResponseEntity<>(account, HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/deposit/{account_number}")
    public ResponseEntity<BankAccountDto> deposit(@PathVariable("account_number") String accountNumber, @RequestBody DepositRequest req) {
        try {
            var result = bankService.deposit(accountNumber, req.amount(), new DepositTransactionStrategy());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof AccountNotFoundException) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/withdraw/{account_number}")
    public ResponseEntity<BankAccountDto> withdraw(@PathVariable("account_number") String accountNumber, @RequestBody WithdrawRequest req) {
        try {
            var result = bankService.withdraw(accountNumber, req.amount(), new WithdrawTransactionStrategy());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof AccountNotFoundException) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else if (e.getCause() instanceof NotEnoughBalanceException) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<List<BankAccountDto>> transferTo(@RequestBody TransferRequest req) {
        try {
            var result = bankService.transferFund(req.origin(), req.dest(), req.amount());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof AccountNotFoundException) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else if (e.getCause() instanceof NotEnoughBalanceException) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/account/balance/{account_number}")
    public ResponseEntity<Double> getBalance(@PathVariable("account_number") String accountNumber) {
        try {
            double balance = bankService.getBalance(accountNumber);
            return new ResponseEntity<>(balance, HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

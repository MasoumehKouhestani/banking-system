package com.azki.banking_system.entities;

import com.azki.banking_system.exceptions.NotEnoughBalanceException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import static com.azki.banking_system.utils.Constants.NOT_ENOUGH_BALANCE_ERROR_MESSAGE;

@Entity
@Table(name = "bank-account")
public class BankAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    @Size(min = 5)
    private String accountNumber;

    @Column
    private double balance;

    @Column(nullable = false)
    @Size(min = 3)
    private String accountHolderName;

    @Column(nullable = false)
    @Size(min = 3)
    private String bankName;

    public BankAccountEntity() {
    }

    public BankAccountEntity(String accountNumber, double balance, String accountHolderName, String bankName) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.accountHolderName = accountHolderName;
        this.bankName = bankName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    @Override
    public String toString() {
        return "BankAccountEntity{" +
                "accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", accountHolderName='" + accountHolderName + '\'' +
                ", bank='" + bankName + '\'' +
                '}';
    }

    public void deposit(double amount) {
        double oldBalance = this.getBalance();

        this.setBalance(oldBalance + amount);
    }

    public void withdraw(double amount) {
        double oldBalance = this.getBalance();

        if (oldBalance < amount) {
            throw new NotEnoughBalanceException(NOT_ENOUGH_BALANCE_ERROR_MESSAGE);
        }

        this.setBalance(oldBalance - amount);
    }
}

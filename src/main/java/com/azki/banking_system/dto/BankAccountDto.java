package com.azki.banking_system.dto;

public record BankAccountDto(Long id, String accountNumber, double balance, String accountHolderName, String bankName) {
}

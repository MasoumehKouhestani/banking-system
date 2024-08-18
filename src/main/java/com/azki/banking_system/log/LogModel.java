package com.azki.banking_system.log;

public record LogModel(String accountNumber, String transactionType, double amount) {
}

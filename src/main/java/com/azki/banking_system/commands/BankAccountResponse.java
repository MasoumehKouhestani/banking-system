package com.azki.banking_system.commands;

import com.azki.banking_system.dto.BankAccountDto;

public record BankAccountResponse(BankAccountDto accountDto, String message) {
}

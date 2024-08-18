package com.azki.banking_system.commands;

import com.azki.banking_system.dto.BankAccountDto;

import java.util.List;

public record TransferResponse(List<BankAccountDto> accountDtos, String message) {
}

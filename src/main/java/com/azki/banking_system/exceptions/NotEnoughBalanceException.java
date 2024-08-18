package com.azki.banking_system.exceptions;

public class NotEnoughBalanceException extends RuntimeException{

    public NotEnoughBalanceException(String message) {
        super(message);
    }
}

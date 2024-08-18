package com.azki.banking_system.controllers;

public record TransferRequest(String origin, String dest, double amount) {
}

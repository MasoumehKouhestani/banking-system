package com.azki.banking_system.log;

import com.azki.banking_system.exceptions.TransactionLogFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

@Component
public class TransactionFileLogger implements TransactionLogger {

    private String filePath;

    @Autowired
    public TransactionFileLogger(@Value("${log.file.path}") String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void onTransaction(String accountNumber, String transactionType, double amount) {
        try (FileWriter fileWriter = new FileWriter(filePath, true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.printf("[%s] --- Account: %s, Transaction: %s, Amount: %.2f\n", new Date(), accountNumber, transactionType, amount);
        } catch (IOException e) {
            throw new TransactionLogFailedException();
        }
    }
}

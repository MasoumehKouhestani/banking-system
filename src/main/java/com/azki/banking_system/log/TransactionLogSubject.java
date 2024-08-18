package com.azki.banking_system.log;

import java.util.ArrayList;
import java.util.List;

public abstract class TransactionLogSubject {
    private final List<TransactionLogger> observers = new ArrayList<>();

    public void addObserver(TransactionLogger observer) {
        observers.add(observer);
    }

    public void removeObserver(TransactionLogger observer) {
        observers.remove(observer);
    }

    protected void notifyObservers(LogModel logModel) {
        for (TransactionLogger observer : observers) {
            observer.onTransaction(logModel.accountNumber(), logModel.transactionType(), logModel.amount());
        }
    }
}

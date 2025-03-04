package org.example.centralserver.services;

import java.util.concurrent.CompletableFuture;

import org.example.centralserver.entities.Account;
import org.example.centralserver.entities.Transection;
import org.example.centralserver.helper.AccountLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class TransactionProcessorService {

    @Autowired
    AccountLoader accountLoader;

    @Async("taskExecutor")
    public CompletableFuture<Void> processTransactionAsync(Transection transaction, String bank) {
        try {
            Object sender = transaction.getSender();
            Object receiver = transaction.getReceiver();

            System.out.println("Processing transaction " + transaction +
                    " on thread: " + Thread.currentThread().getName());


            Account senderAccount = accountLoader.loadAccountIntoRedis(sender, transaction, bank);
            Account receiverAccount = accountLoader.loadAccountIntoRedis(receiver, transaction, bank);


            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
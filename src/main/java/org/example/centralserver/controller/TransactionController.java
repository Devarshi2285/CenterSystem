package org.example.centralserver.controller;



import org.example.centralserver.entities.Transection;
import org.example.centralserver.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public void fetchTransactions() {
        System.out.println("Fetching transactions");
        transactionService.processTransactions();
    }
}

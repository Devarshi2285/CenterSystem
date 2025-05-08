package org.example.centralserver.controller;



import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.centralserver.entities.Transection;
import org.example.centralserver.repo.mongo.TransectionRepo;
import org.example.centralserver.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransectionRepo transectionRepo;

    @GetMapping
    public void fetchTransactions() throws Exception {
        System.out.println("Fetching transactions");
        transactionService.processTransactions();
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Transection>> getAllTransactions() throws JsonProcessingException {
        List<Transection>transectionList=transectionRepo.findAll();
        return ResponseEntity.ok(transectionList);
    }

}

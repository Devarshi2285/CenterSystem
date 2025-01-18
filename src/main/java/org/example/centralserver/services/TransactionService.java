package org.example.centralserver.services;

import org.example.centralserver.entities.Account;
import org.example.centralserver.entities.AccountNeo4J;
import org.example.centralserver.entities.Transection;
import org.example.centralserver.helper.AccountLoader;
import org.example.centralserver.helper.GetAccounts;
import org.example.centralserver.mapper.Bank1TransactionMapper;
import org.example.centralserver.repo.neo4j.AccountNeo4jRepository;
import org.example.centralserver.repo.mongo.AccountRepo;
import org.example.centralserver.repo.mongo.TransectionRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;

@Service
public class TransactionService {

    private static final int BATCH_SIZE = 1000;

    private static ModelMapper modelMapper = new ModelMapper();


    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AccountNeo4jRepository accountNeo4jRepository;

    @Autowired
    private TransectionRepo transectionRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private Bank1TransactionMapper bank1TransactionMapper;

    @Autowired
    private GetAccounts getAccounts;

    @Autowired
    TransactionProcessorService transactionProcessorService;

    List<CompletableFuture<Void>> futures = new ArrayList<>();

    @Autowired
    RedisService redisService;

    private final String bankApiUrl = "http://localhost:8080/transactions"; // Replace with actual API URL

    // Scheduled task to fetch and process transactions every 5 minutes
    @Scheduled(fixedRate = 6000000) //1 min
    public void processTransactions() throws InterruptedException {
        System.out.println("Fetching transactions from bank API...");

        // Fetch transactions from the bank's API
        List<?> response = restTemplate.getForObject(bankApiUrl, List.class);
        List<Transection> transactions = bank1TransactionMapper.mapTransactions(response);


        int totalTransactions = transactions.size();
        int processedCount = 0;

        while (processedCount < totalTransactions) {
            List<CompletableFuture<Void>> batchFutures = new ArrayList<>();

            // Process one batch
            int endIndex = Math.min(processedCount + BATCH_SIZE, totalTransactions);
            for (int i = processedCount; i < endIndex; i++) {
                CompletableFuture<Void> future = transactionProcessorService.processTransactionAsync(transactions.get(i), "bank1");
                batchFutures.add(future);
            }

            // Wait for batch completion
            CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0])).join();
            processedCount = endIndex;
        }
    }


    private void createTransactionRelationship(AccountNeo4J sender, AccountNeo4J receiver, Transection transaction) {
        // Define a relationship, e.g., "SENT"
        sender.addTransactionTo(receiver, transaction);
        accountNeo4jRepository.save(sender);
    }

    private void loadIntoDb() {

    }

}

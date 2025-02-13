package org.example.centralserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.centralserver.entities.Account;
import org.example.centralserver.entities.AccountNeo4J;
import org.example.centralserver.entities.Transection;
import org.example.centralserver.entities.config.BankConfig;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;

@Service
public class TransactionService {

    private static final int BATCH_SIZE = 10000;



    @Autowired
    TransactionProcessorService transactionProcessorService;
    @Autowired
    BankConfigService bankConfigService;
    @Autowired
    TransformData transformData;


    List<CompletableFuture<Void>> futures = new ArrayList<>();


    // Scheduled task to fetch and process transactions every 5 minutes
    @Scheduled(fixedRate = 6000000) //1 min
    public void processTransactions() throws InterruptedException, JsonProcessingException {
        System.out.println("Fetching transactions from bank API...");

        Instant startTime = Instant.now();
        // Fetch transactions from the bank's API

        List<BankConfig> banks = bankConfigService.getAllBankConfig();


        banks.forEach(bankConfig -> {
            transformData.convertAndProcessData(bankConfig);
        });


//        List<?> response = restTemplate.getForObject(bankApiUrl, List.class);
//        List<Transection> transactions = bank1TransactionMapper.mapTransactions(response,"bank1");
//
//
//        int totalTransactions = transactions.size();
//        int processedCount = 0;
//
//        while (processedCount < totalTransactions) {
//            List<CompletableFuture<Void>> batchFutures = new ArrayList<>();
//
//            // Process one batch
//            int endIndex = Math.min(processedCount + BATCH_SIZE, totalTransactions);
//            for (int i = processedCount; i < endIndex; i++) {
//                CompletableFuture<Void> future = transactionProcessorService.processTransactionAsync(transactions.get(i), "bank1");
//                batchFutures.add(future);
//            }
//
//            // Wait for batch completion
//            CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0])).join();
//            processedCount = endIndex;
//        }
//        Instant endTime = Instant.now();
//
//        // Calculate elapsed time in milliseconds
//        long elapsedTime = ChronoUnit.MILLIS.between(startTime, endTime);
//
//        // Print elapsed time
//        System.out.println("Transaction processing completed in: " + elapsedTime + " milliseconds");
//        //loadIntoDb(transactions,"bank1");
//    }
//
//
//    private void createTransactionRelationship(AccountNeo4J sender, AccountNeo4J receiver, Transection transection) {
//        // Define a relationship, e.g., "SENT"
//
//        try {
//            sender.addTransactionTo(receiver, transection);
//            accountNeo4jRepository.save(sender);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void loadIntoDb(List<Transection> transactions , String bank) throws JsonProcessingException {
//
//        int count=0;
//        Set<Object> accountKeys = redisService.getSetMembers("accounts");
//
//
//        if (accountKeys != null) {
//            // For each account key, retrieve the associated account object
//            for (Object accountKey : accountKeys) {
//                String accountKey1=accountKey.toString();
//                Account account = redisService.getObject(accountKey1, Account.class);  // Retrieve the account object
//
//                accountService.addaccount(account);
//                if(account.isSuspicious()){
//
//                    ModelMapper modelMapper = new ModelMapper();
//
//                    // Convert Account to AccountNeo4J
//                    AccountNeo4J accountNeo4J = modelMapper.map(account, AccountNeo4J.class);
//
//                    accountNeo4jRepository.save(accountNeo4J);
//
//                }
//
//                count+=account.getFreq();
//            }
//        }
//
//        Set<Object> transectionKeys = redisService.getSetMembers("transaction");
//
//
//        if (transectionKeys != null) {
//            // For each account key, retrieve the associated account object
//            for (Object transectionKey : transectionKeys) {
//                String transectionKey1=transectionKey.toString();
//                Transection transection = redisService.getObject(transectionKey1, Transection.class);  // Retrieve the account object
//
//
//
//                transectionRepo.save(transection);
//
//                AccountNeo4J sender=accountNeo4jRepository.findById(bank+"_"+transection.getSender()).orElse(null);
//                AccountNeo4J receiver=accountNeo4jRepository.findById(bank+"_"+transection.getReceiver()).orElse(null);
//
//
//                if(sender==null){
//                    accountNeo4jRepository.save(sender);
//                }
//                if (receiver==null){
//                    accountNeo4jRepository.save(receiver);
//                }
//                createTransactionRelationship(sender, receiver, transection);
//
//            }
//        }
//            System.out.println("Total accounts: " + count);
//
//
//    }

    }

}

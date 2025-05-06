package org.example.centralserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.centralserver.entities.Account;
import org.example.centralserver.entities.Transection;
import org.example.centralserver.entities.TransectionUser;
import org.example.centralserver.entities.User;
import org.example.centralserver.entities.config.BankConfig;
import org.example.centralserver.entities.neo4j.AccountNode;
import org.example.centralserver.entities.neo4j.TransactionRelationship;
import org.example.centralserver.repo.mongo.AccountRepo;
import org.example.centralserver.repo.mongo.TransectionRepo;
import org.example.centralserver.repo.mongo.UserRepo;
import org.example.centralserver.repo.neo4j.AccountNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
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

    @Autowired
    RedisService redisService;

    List<CompletableFuture<Void>> futures = new ArrayList<>();
    @Autowired
    private TransectionRepo transectionRepo;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private AccountNodeRepository accountNodeRepo;
    @Autowired
    private UserRepo userRepo;


    @Scheduled(fixedRate = 6000000)
    public void processTransactions() throws InterruptedException, JsonProcessingException {
        System.out.println("Fetching transactions from bank API...");

        Instant startTime = Instant.now();
        // Fetch transactions from the bank's API

        List<BankConfig> banks = bankConfigService.getAllBankConfig();
        List<List<Transection>>allTransactions = new ArrayList<>();

        banks.forEach(bankConfig -> {
            if(Objects.equals(bankConfig.getDatabaseStructure(), "NOSQL")) {
                List<Transection> transectionList = transformData.convertAndProcessData(bankConfig);
                allTransactions.add(transectionList);
            }

            else {

                List<Transection> transectionList = transformData.convertAndProcessData(bankConfig);

            }

        });


        for (List<Transection> transectionList : allTransactions) {
            int totalTransactions = transectionList.size();
            int processedCount = 0;

            //replace by with chunk

            while (processedCount < totalTransactions) {
                List<CompletableFuture<Void>> batchFutures = new ArrayList<>();

                // Process one batch
                int endIndex = Math.min(processedCount + BATCH_SIZE, totalTransactions);
                for (int i = processedCount; i < endIndex; i++) {
                    CompletableFuture<Void> future = transactionProcessorService.processTransactionAsync(transectionList.get(i), banks.get(0).getBankId());
                    batchFutures.add(future);
                }

                // Wait for batch completion
                CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0])).join();
                processedCount = endIndex;
            }



        }

        loadIntoDb();



    }

    private void loadIntoDb() {

        // Move all accounts from Redis to accountRepo
        Set<Object> accountKeys = redisService.getSetMembers("accounts");
        if (accountKeys != null) {
            for (Object accountKey : accountKeys) {
                Account account = redisService.getObject(accountKey.toString(), Account.class);


                Optional<AccountNode> existingAccountNode = accountNodeRepo.findByAccountNumber(account.getAccountNumber());


                AccountNode accountNode;

                if (existingAccountNode.isPresent()) {
                    // If node exists, update the existing node
                    accountNode = existingAccountNode.get();

                    // Update all fields of the existing node
                    accountNode.setBalance(account.getBalance());
                    accountNode.setFreq((int)account.getFreq());
                    accountNode.setUser(account.getUser());
                    accountNode.setType(account.getType());
                    accountNode.setRegularIntervalTransaction(account.getRegularIntervelTransection());
                    accountNode.setSuspicious(account.getSuspicious());
                } else {
                    // If no existing node, create a new one
                    accountNode = new AccountNode();
                    accountNode.setAccountNumber(account.getAccountNumber());
                    accountNode.setBalance(account.getBalance());
                    accountNode.setFreq((int)account.getFreq());
                    accountNode.setUser(account.getUser());
                    accountNode.setType(account.getType());
                    accountNode.setRegularIntervalTransaction(account.getRegularIntervelTransection());
                    accountNode.setSuspicious(account.getSuspicious());
                }

                // Save the node (will update if exists, create if new)
                accountNodeRepo.save(accountNode);

                // Also save the original account


                accountRepo.save(account);
            }
        }

        // Move all transactions from Redis to transectionRepo
        Set<Object> transectionKeys = redisService.getSetMembers("transaction");
        if (transectionKeys != null) {
            for (Object transectionKey : transectionKeys) {
                Transection transaction = redisService.getObject(transectionKey.toString(), Transection.class);

                TransectionUser sender=(TransectionUser) transaction.getSender();
                TransectionUser receiver=(TransectionUser) transaction.getReceiver();

                Account senderAcc=(Account)sender.getAccount();
                Account receiverAcc=(Account)receiver.getAccount();

                AccountNode senderNode = accountNodeRepo.findByAccountNumber(senderAcc.getAccountNumber()).orElseThrow();
                AccountNode receiverNode = accountNodeRepo.findByAccountNumber(receiverAcc.getAccountNumber()).orElseThrow();


                TransactionRelationship transactionEdge = new TransactionRelationship();

                transactionEdge.setAmt(transaction.getAmt());
                transactionEdge.setCurrency(transaction.getCurrency());
                transactionEdge.setCreatedDate(transaction.getCreatedDate());
                transactionEdge.setType(transaction.getType());
                transactionEdge.setDescription(transaction.getDescription());
                transactionEdge.setTargetAccount(receiverNode);
                senderNode.getOutgoingTransactions().add(transactionEdge);


//                String url = "http://localhost:5000/predict"; // Flask running locally
//
//                RestTemplate restTemplate = new RestTemplate();
//                Map<String, Object> request = new HashMap<>();
//                request.put("features", transaction);
//
//                ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
//
//                System.out.println(Objects.requireNonNull(response.getBody()).get("prediction").toString());


                accountNodeRepo.save(senderNode);

                User senderuser=sender.getUser();
                User reciveruser=receiver.getUser();

                Account senderacc=sender.getAccount();
                Account receiveracc=receiver.getAccount();

                senderacc.setUserclass(senderuser);
                receiveracc.setUserclass(reciveruser);

                accountRepo.save(senderacc);
                accountRepo.save(receiveracc);

                transectionRepo.save(transaction);
            }
        }
    }

}

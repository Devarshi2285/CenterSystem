package org.example.centralserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.centralserver.entities.Account;
import org.example.centralserver.entities.Transection;
import org.example.centralserver.entities.TransectionUser;
import org.example.centralserver.entities.config.BankConfig;
import org.example.centralserver.entities.neo4j.AccountNode;
import org.example.centralserver.entities.neo4j.TransactionRelationship;
import org.example.centralserver.repo.mongo.AccountRepo;
import org.example.centralserver.repo.mongo.TransectionRepo;
import org.example.centralserver.repo.neo4j.AccountNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
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

    @Autowired
    RedisService redisService;

    List<CompletableFuture<Void>> futures = new ArrayList<>();
    @Autowired
    private TransectionRepo transectionRepo;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private AccountNodeRepository accountNodeRepo;

    // Scheduled task to fetch and process transactions every 5 minutes
    @Scheduled(fixedRate = 6000000) //1 min
    public void processTransactions() throws InterruptedException, JsonProcessingException {
        System.out.println("Fetching transactions from bank API...");

        Instant startTime = Instant.now();
        // Fetch transactions from the bank's API

        List<BankConfig> banks = bankConfigService.getAllBankConfig();
        List<List<Transection>>allTransactions = new ArrayList<>();

        banks.forEach(bankConfig -> {
            List<Transection> transectionList=transformData.convertAndProcessData(bankConfig);
            allTransactions.add(transectionList);
        });


        for (List<Transection> transectionList : allTransactions) {
            int totalTransactions = transectionList.size();
            int processedCount = 0;

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


                // Save only the sender node, it will also save the relationship
                accountNodeRepo.save(senderNode);

                transectionRepo.save(transaction);
            }
        }
    }

}

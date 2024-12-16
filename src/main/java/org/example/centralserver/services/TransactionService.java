package org.example.centralserver.services;

import org.example.centralserver.entities.Account;
import org.example.centralserver.entities.Transection;
import org.example.centralserver.helper.AccountLoader;
import org.example.centralserver.helper.GetAccounts;
import org.example.centralserver.mapper.Bank1TransactionMapper;
import org.example.centralserver.repo.AccountRepo;
import org.example.centralserver.repo.TransectionRepo;
import org.example.centralserver.utils.RestClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AccountLoader accountLoader;


    @Autowired
    private TransectionRepo transectionRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private Bank1TransactionMapper bank1TransactionMapper;

    @Autowired
    private GetAccounts getAccounts;

    @Autowired
    RedisService redisService;

    private final String bankApiUrl = "http://localhost:8080/transactions"; // Replace with actual API URL

    // Scheduled task to fetch and process transactions every 5 minutes
    @Scheduled(fixedRate = 60000) //1 min
    public void processTransactions() {
        System.out.println("Fetching transactions from bank API...");

        // Fetch transactions from the bank's API
        List<?> response = restTemplate.getForObject(bankApiUrl, List.class);
        List<Transection> transactions = bank1TransactionMapper.mapTransactions(response);

        for (Transection transaction : transactions) {
            transectionRepo.save(transaction);
            processTransaction(transaction);
        }
    }

    // Process each transaction
    private void processTransaction(Transection transaction) {
        String sender = transaction.getSender();
        String receiver = transaction.getReceiver();

        // Process sender account
        Account senderAccount = accountLoader.loadAccountIntoRedis(sender, transaction);

        // Process receiver account
        Account receiverAccount = accountLoader.loadAccountIntoRedis(receiver, transaction);

        // Check for suspicious activity
        boolean isSenderSuspicious = checkSuspiciousAccount(senderAccount, transaction);
        boolean isReceiverSuspicious = checkSuspiciousAccount(receiverAccount, transaction);

        //Save suspicious accounts and transactions to Redis, and others to MongoDB
        if(transaction.getAmt() > 2000){
            redisService.saveObject(transaction.getId(), transaction);
        }
        if (isSenderSuspicious || isReceiverSuspicious) {

        } else {
            redisService.deleteKey(sender);
            redisService.deleteKey(receiver);

        }


        accountRepo.save(senderAccount);
        accountRepo.save(receiverAccount);
    }

        // Fetch account from Redis or database, update, and return

    // Check if the account is suspicious based on the criteria
    private boolean checkSuspiciousAccount(Account account, Transection transaction) {
        boolean isSuspicious = false;

        // Check if frequency exceeds 50
        if (account.getFreq() > 10) {
            isSuspicious = true;
        }

        // Check if the gap between last transaction and today is more than a year
        if (account.getLastTransaction() != null) {
            long yearsGap = ChronoUnit.YEARS.between(account.getLastTransaction(), LocalDateTime.now());
            if (yearsGap > 1) {
                isSuspicious = true;
            }
        }

        return isSuspicious;
    }
}

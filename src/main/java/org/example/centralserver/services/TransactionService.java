package org.example.centralserver.services;

import org.example.centralserver.entities.Account;
import org.example.centralserver.entities.Transection;
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
    private TransectionRepo transectionRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private Bank1TransactionMapper bank1TransactionMapper;

    @Autowired
    private GetAccounts getAccounts;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String bankApiUrl = "http://localhost:8080/transactions"; // Replace with actual API URL

    // Scheduled task to fetch and process transactions every 5 minutes
    @Scheduled(fixedRate = 60000) //1min
    public void processTransactions() {
        System.out.println("Fetching transactions from bank API...");

        // Fetch transactions from the bank's API
        List<?> response = restTemplate.getForObject(bankApiUrl, List.class);
        List<Transection> transactions = bank1TransactionMapper.mapTransactions(response);

        for (Transection transaction : transactions) {
            processTransaction(transaction);
        }
    }

    // Process each transaction
    private void processTransaction(Transection transaction) {
        String sender = transaction.getSender();
        String receiver = transaction.getReceiver();

        // Process sender account
        Account senderAccount = getAndUpdateAccount(sender, transaction);

        // Process receiver account
        Account receiverAccount = getAndUpdateAccount(receiver, transaction);

        // Check for suspicious activity
        boolean isSenderSuspicious = checkSuspiciousAccount(senderAccount, transaction);
        boolean isReceiverSuspicious = checkSuspiciousAccount(receiverAccount, transaction);

        // Save suspicious accounts and transactions to Redis, and others to MongoDB
        if (isSenderSuspicious || isReceiverSuspicious || transaction.getAmt() > 500000) {
            redisTemplate.opsForHash().put("suspicious_transactions", transaction.getId(), transaction);
            redisTemplate.opsForHash().put("suspicious_accounts", senderAccount.getId(), senderAccount);
            redisTemplate.opsForHash().put("suspicious_accounts", receiverAccount.getId(), receiverAccount);
        } else {
            transectionRepo.save(transaction);
            accountRepo.save(senderAccount);
            accountRepo.save(receiverAccount);
        }
    }

    // Fetch account from Redis or database, update, and return
    private Account getAndUpdateAccount(String accId, Transection transaction) {
        Account account = (Account) redisTemplate.opsForHash().get("accounts", accId);

        if (account == null) {
            Optional<Account> optionalAccount = accountRepo.findByAccId(accId);
            account = optionalAccount.orElseGet(() -> getAccounts.getAccount(accId));
            redisTemplate.opsForHash().put("accounts", accId, account);
        }

        // Update account details
        account.setLastTransaction(transaction.getCreatedDate());
        account.setFreq(account.getFreq() + 1);

        return account;
    }

    // Check if the account is suspicious based on the criteria
    private boolean checkSuspiciousAccount(Account account, Transection transaction) {
        boolean isSuspicious = false;

        // Check if frequency exceeds 50
        if (account.getFreq() > 50) {
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

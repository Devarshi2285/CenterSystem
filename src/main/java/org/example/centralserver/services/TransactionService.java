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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TransactionService {


    private static ModelMapper modelMapper = new ModelMapper();


    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AccountNeo4jRepository accountNeo4jRepository;

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
            processTransaction(transaction,"bank1");
        }

        loadIntoDb();
    }

    // Process each transaction
    private void processTransaction(Transection transaction,String bank) {
        String sender = transaction.getSender();
        String receiver = transaction.getReceiver();

        // Process sender account
        Account senderAccount = accountLoader.loadAccountIntoRedis(sender, transaction, bank);

        // Process receiver account
        Account receiverAccount = accountLoader.loadAccountIntoRedis(receiver, transaction, bank);

        // Check for suspicious activity
        boolean isSenderSuspicious = checkSuspiciousAccount(senderAccount, transaction);
        boolean isReceiverSuspicious = checkSuspiciousAccount(receiverAccount, transaction);


        //Save suspicious accounts and transactions to Redis, and others to MongoDB
        if (transaction.getAmt() > 2000) {
            senderAccount.setSuspicious(true);
            receiverAccount.setSuspicious(true);
            redisService.saveObject(bank + "_" + sender, senderAccount);
            redisService.saveObject(bank + "_" + receiver, receiverAccount);
            redisService.saveObject(transaction.getId(), transaction);

            redisService.addToSet("accounts", bank + "_" + sender);
            redisService.addToSet("accounts", bank + "_" + receiver);
            redisService.addToSet("transaction", transaction.getId());
        } else if (isSenderSuspicious || isReceiverSuspicious) {
            senderAccount.setSuspicious(true);
            receiverAccount.setSuspicious(true);
            redisService.saveObject(bank + "_" + sender, senderAccount);
            redisService.saveObject(bank + "_" + receiver, receiverAccount);
        }
    }

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

    private void createTransactionRelationship(AccountNeo4J sender, AccountNeo4J receiver, Transection transaction) {
        // Define a relationship, e.g., "SENT"
        sender.addTransactionTo(receiver, transaction);
        accountNeo4jRepository.save(sender);
    }

    private void loadIntoDb() {

    }

}

package org.example.centralserver.helper;

import lombok.extern.slf4j.Slf4j;
import org.example.centralserver.entities.Account;
import org.example.centralserver.entities.Transection;
import org.example.centralserver.entities.TransectionUser;
import org.example.centralserver.services.AccountService;
import org.example.centralserver.services.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class AccountLoader {
    private static final Map<String, Lock> accountLocks = new ConcurrentHashMap<>();
    private static final long LOCK_TIMEOUT_MS = 5000; // Reduced to 5 seconds
    private final int MAX_RETRY_ATTEMPTS = 3;

    @Autowired
    private RedisService redisService;

    @Autowired
    private AccountService accountService;
    

    public Account loadAccountIntoRedis(TransectionUser acc, Transection transection, String bank , Boolean isSender) {
        String lockKey = bank + "_" + acc.getAccount().getId();
        int attempts = 0;


        //storing log of skipped transaction

        String accId=acc.getAccount().getId();
        while (attempts < MAX_RETRY_ATTEMPTS) {
            Lock accountLock = accountLocks.computeIfAbsent(lockKey, k -> new ReentrantLock(true)); // Fair locking

            try {
                if (accountLock.tryLock(LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    try {
                        return processAccount(lockKey, accId, transection, bank , isSender);
                    } finally {
                        accountLock.unlock();
                        // Clean up if no other threads are waiting
                        if (((ReentrantLock) accountLock).getQueueLength() == 0) {
                            accountLocks.remove(lockKey);
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while processing account: " + accId);
            }

            attempts++;
            if (attempts < MAX_RETRY_ATTEMPTS) {
                // Add small random delay before retry
                try {
                    Thread.sleep(100 + (long)(Math.random() * 400));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw new RuntimeException("Could not acquire lock for account: " + accId + " after " + MAX_RETRY_ATTEMPTS + " attempts");
    }

    private Account processAccount(String redisKey, String accId, Transection transection, String bank , Boolean isSender) {
        // First try Redis
        Account account = redisService.getObject(redisKey, Account.class);

        // Batch fetch if not in Redis
        if (account == null) {
            TransectionUser transectionUser;
            if(isSender){
                transectionUser = (TransectionUser) transection.getSender();
            }
           else {
                transectionUser = (TransectionUser) transection.getReceiver();
            }
            account=transectionUser.getAccount();
        }

        updateAccountDetails(account, transection, redisKey , isSender);
        return account;
    }

    private void updateAccountDetails(Account account, Transection transection, String redisKey , boolean isSender) {

        Long newFreq = redisService.increment(redisKey + ":freq");
        account.setFreq(newFreq.intValue());

        account.setLastTransaction(transection.getCreatedDate());


        if (transection.getAmt() > 5000 ) {
            transection.setSuspicious(true);
            account.setSuspicious(true);
        }

        if(account.getFreq() > 10 ){
            account.setSuspicious(true);
        }
        redisService.saveObject(redisKey, account);
        redisService.addToSet("accounts", redisKey);

        TransectionUser user;

        if(isSender){
            user = (TransectionUser) transection.getSender();
        }

        else {
           user = (TransectionUser) transection.getReceiver();
        }
        user.setAccount(account);

        if (isSender){
            transection.setSender(user);
        }
        else{
            transection.setReceiver(user);
        }
        System.out.println(transection.getId());

        redisService.saveObject(transection.getId(), transection);

        redisService.addToSet("transaction", transection.getId());
    }
}
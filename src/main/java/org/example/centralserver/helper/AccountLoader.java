package org.example.centralserver.helper;

import lombok.extern.slf4j.Slf4j;
import org.example.centralserver.entities.Account;
import org.example.centralserver.entities.Transection;
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

    @Autowired
    private GetAccounts getAccounts;

    public Account loadAccountIntoRedis(Object acc, Transection transection, String bank) {
        String lockKey = bank + "_" + acc.toString();
        int attempts = 0;

        String accId="0";
        while (attempts < MAX_RETRY_ATTEMPTS) {
            Lock accountLock = accountLocks.computeIfAbsent(lockKey, k -> new ReentrantLock(true)); // Fair locking

            try {
                if (accountLock.tryLock(LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    try {
                        return processAccount(lockKey, accId, transection, bank);
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

    private Account processAccount(String redisKey, String accId, Transection transection, String bank) {
        // First try Redis
        Account account = redisService.getObject(redisKey, Account.class);

        // Batch fetch if not in Redis
        if (account == null) {
            account = fetchAndCacheAccount(accId, bank, redisKey);
        }

        updateAccountDetails(account, transection, redisKey);
        return account;
    }

    private Account fetchAndCacheAccount(String accId, String bank, String redisKey) {
        Account account = accountService.getaccount(redisKey);
        if (account == null) {
            account = getAccounts.getAccount(accId, bank);
            if (account == null) {
                throw new RuntimeException("Account not found: " + accId);
            }
        }
        return account;
    }

    private void updateAccountDetails(Account account, Transection transection, String redisKey) {

        Long newFreq = redisService.increment(redisKey + ":freq");
        account.setFreq(newFreq.intValue());

        account.setLastTransaction(transection.getCreatedDate());


        if (transection.getAmt() > 5000 || account.getFreq() > 10) {
            account.setSuspicious(true);
        }

        redisService.saveObject(redisKey, account);
        redisService.addToSet("accounts", redisKey);
        System.out.println(transection.getId());
        redisService.saveObject(transection.getId(), transection);

        redisService.addToSet("transaction", transection.getId());
    }
}
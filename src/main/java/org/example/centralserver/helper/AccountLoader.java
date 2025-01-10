package org.example.centralserver.helper;

import org.example.centralserver.entities.Account;
import org.example.centralserver.entities.Transection;
import org.example.centralserver.services.AccountService;
import org.example.centralserver.services.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountLoader {

    @Autowired
    private RedisService redisService;

    @Autowired
    private AccountService accountService;

    @Autowired
    GetAccounts getAccounts;

    public Account loadAccountIntoRedis(String accId, Transection transection, String bank) {
        try {
            String redisKey = bank + "_" + accId;
            Account account = redisService.getObject(redisKey, Account.class);

            if (account == null) {
                account = accountService.getaccount(redisKey);
                if (account == null) {
                    account = getAccounts.getAccount(accId, bank);
                    if (account == null) {
                        throw new RuntimeException("Account not found in any service for accId: " + accId);
                    }
                }
            }

            // Update account details
            account.setLastTransaction(transection.getCreatedDate());
            account.setFreq(account.getFreq() + 1);

            // Save back to Redis
            redisService.saveObject(redisKey, account);
            return account;
        } catch (Exception e) {
            System.out.println("Error while loading account into Redis: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


}

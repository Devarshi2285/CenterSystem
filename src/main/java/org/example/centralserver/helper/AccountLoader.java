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

    public Account loadAccountIntoRedis(String accId , Transection transection){

        try {


            Account account = redisService.getObject(accId, Account.class);

            if (account == null) {
                Account account1 = accountService.getAccountByAccId(accId);
                if (account1 == null) {
                    account1 = getAccounts.getAccount(accId);
                }

                account1.setLastTransaction(transection.getCreatedDate());
                account1.setFreq(account1.getFreq() + 1);

                redisService.saveObject(accId, account1);
                return account1;
            }

            account.setLastTransaction(transection.getCreatedDate());
            account.setFreq(account.getFreq() + 1);
            redisService.saveObject(accId, account);
            return account;

        }
        catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }


}

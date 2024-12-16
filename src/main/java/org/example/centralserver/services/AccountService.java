package org.example.centralserver.services;

import org.example.centralserver.entities.Account;
import org.example.centralserver.repo.AccountRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;

@Service
public class AccountService {

    @Autowired
    public AccountRepo accountRepo;

    public AccountService(AccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    public Account addaccount(Account account) {
        return accountRepo.save(account);
    }
    public Account getaccount(String id) throws AccountNotFoundException {
        return accountRepo.findByAccId(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    public Account getAccountByAccId(String accId) {
        return accountRepo.findByAccId(accId).orElse(null);
    }


}

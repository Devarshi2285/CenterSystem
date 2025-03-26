package org.example.centralserver.controller;

import org.example.centralserver.entities.Account;
import org.example.centralserver.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*")

public class AccountController {

    @Autowired
    AccountService accountService;


    @GetMapping
    public ResponseEntity<List<Account>> getAccounts() {

        List<Account>accounts=accountService.getaccounts();

        return ResponseEntity.ok(accounts);

    }
}

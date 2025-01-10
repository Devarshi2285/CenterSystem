package org.example.centralserver.helper;

import org.example.centralserver.entities.Account;
import org.example.centralserver.mapper.Bank1AccountMapper;
import org.example.centralserver.mapper.Bank1TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class GetAccounts {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Bank1AccountMapper bank1AccountMapper;

    @Autowired
    private Bank1TransactionMapper bank1TransactionMapper;

    private final String baseApiUrl = "http://localhost:8080/accounts/"; // Base API URL

    public Account getAccount(String accountId,String bank) {
        String requestUrl = baseApiUrl + accountId; // Construct URL dynamically
        Map<String, Object> accountData = restTemplate.getForObject(requestUrl, Map.class);
        return bank1AccountMapper.mapAccount(accountData,bank);
    }
}

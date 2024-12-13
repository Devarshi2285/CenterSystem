package org.example.centralserver.mapper;

import org.example.centralserver.entities.Account;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class Bank1AccountMapper {

    // Method to map a single account using the constructor
    public Account mapAccount(Map<String, Object> accountData) {
        // Extract data from the response map
        String id = (String) accountData.get("id");
        String user = (String) accountData.get("user");
        List<String> nomineeIds = (List<String>) accountData.get("nominees");

        // Create Account objects for nominees (only IDs for now)

        // Using the constructor to create an Account object
        Account account = new Account(
                id,
                "1",                  // Hardcoded bank ID for now, set dynamically if needed
                user,                 // User ID
                nomineeIds     // List of nominee accounts
        );

        return account;
    }
}
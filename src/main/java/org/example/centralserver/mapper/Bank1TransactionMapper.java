package org.example.centralserver.mapper;

import org.example.centralserver.entities.Transection;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class Bank1TransactionMapper implements BankTransactionMapper{
    @Override
    public List<Transection> mapTransactions(List<?> bankData) {
        List<Transection> transactions = new ArrayList<>();

        for (Object obj : bankData) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) obj;

            Transection transaction = new Transection(
                    (String) data.get("sender"),
                    (String) data.get("receiver"),
                    (Double) data.get("amount"),
                    "1", // Hardcoded sender bank for now, update dynamically as needed
                    (String) data.get("receiverBankId"),
                    LocalDateTime.parse((String)data.get("createdAt"))

            );

            transactions.add(transaction);
        }

        return transactions;
    }
}

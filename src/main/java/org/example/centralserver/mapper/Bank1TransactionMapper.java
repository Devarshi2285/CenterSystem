package org.example.centralserver.mapper;

import org.example.centralserver.entities.Transection;
import org.example.centralserver.entities.TransectionUser;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class Bank1TransactionMapper implements BankTransactionMapper{
    @Override
    public List<Transection> mapTransactions(List<?> bankData,String bank) {
        List<Transection> transactions = new ArrayList<>();

        for (Object obj : bankData) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) obj;

            Transection transaction = new Transection(
                    bank+(String) data.get("id"),
                    (TransectionUser) data.get("sender"),
                    (TransectionUser) data.get("receiver"),
                    (Double) data.get("amount"),
                    (String) data.get("type"),
                    (String) data.get("currency"),
                    (String) data.get("notes"),
                    (Double) data.get("balanceAfterTransection"),
                    LocalDateTime.parse((String)data.get("createdAt"))
            );

            transactions.add(transaction);
        }

        return transactions;
    }
}

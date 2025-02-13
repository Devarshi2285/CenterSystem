package org.example.centralserver.mapper;

import org.example.centralserver.entities.Transection;

import java.util.List;

public interface BankTransactionMapper {
    List<Transection> mapTransactions(List<?> bankData,String bank);
}


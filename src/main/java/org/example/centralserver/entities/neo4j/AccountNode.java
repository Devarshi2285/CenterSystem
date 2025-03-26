package org.example.centralserver.entities.neo4j;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("Account")
public class AccountNode {
    @Id
    @GeneratedValue
    private Long id;

    private String accountNumber;
    private String type;
    private Double balance;
    private String user;
    private Integer freq;
    private Integer regularIntervalTransaction;
    private Boolean isSuspicious;

    @Relationship(type = "TRANSACTS_WITH", direction = Relationship.Direction.OUTGOING)
    private Set<TransactionRelationship> outgoingTransactions = new HashSet<>();

    @Relationship(type = "TRANSACTS_WITH", direction = Relationship.Direction.INCOMING)
    private Set<TransactionRelationship> incomingTransactions = new HashSet<>();

    public AccountNode() {}


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Integer getFreq() {
        return freq;
    }

    public void setFreq(Integer freq) {
        this.freq = freq;
    }

    public Integer getRegularIntervalTransaction() {
        return regularIntervalTransaction;
    }

    public void setRegularIntervalTransaction(Integer regularIntervalTransaction) {
        this.regularIntervalTransaction = regularIntervalTransaction;
    }

    public Boolean getSuspicious() {
        return isSuspicious;
    }

    public void setSuspicious(Boolean suspicious) {
        isSuspicious = suspicious;
    }

    public Set<TransactionRelationship> getOutgoingTransactions() {
        return outgoingTransactions;
    }

    public void setOutgoingTransactions(Set<TransactionRelationship> outgoingTransactions) {
        this.outgoingTransactions = outgoingTransactions;
    }

    public Set<TransactionRelationship> getIncomingTransactions() {
        return incomingTransactions;
    }

    public void setIncomingTransactions(Set<TransactionRelationship> incomingTransactions) {
        this.incomingTransactions = incomingTransactions;
    }
}
package org.example.centralserver.entities.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.neo4j.driver.TransactionConfig;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

@Document("BankConfig")
@Component
public class BankConfig {

    private String bankId;
    private AccountConfig accountConfig;
    private TransectionConfig transectionConfig;
    private TransectionUserConfig transectionUserConfig;
    private UserConfig userConfig;
    private String accountURI;
    private String transactionURI;
    private String userURI;
    private String databaseStructure;

    public String getDatabaseStructure() {
        return databaseStructure;
    }

    public String getBankId() {
        return bankId;
    }

    public AccountConfig getAccountConfig() {
        return accountConfig;
    }

    public TransectionConfig getTransactionConfig() {
        return transectionConfig;
    }

    public TransectionUserConfig getTransectionUserConfig() {
        return transectionUserConfig;
    }

    public UserConfig getUserConfig() {
        return userConfig;
    }

    public String getAccountURI() {
        return accountURI;
    }

    public String getTransactionURI() {
        return transactionURI;
    }

    public String getUserURI() {
        return userURI;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public void setAccountConfig(AccountConfig accountConfig) {
        this.accountConfig = accountConfig;
    }

    public void setTransectionConfig(TransectionConfig transectionConfig) {
        this.transectionConfig = transectionConfig;
    }

    public void setTransectionUserConfig(TransectionUserConfig transectionUserConfig) {
        this.transectionUserConfig = transectionUserConfig;
    }

    public void setUserConfig(UserConfig userConfig) {
        this.userConfig = userConfig;
    }

    public void setAccountURI(String accountURI) {
        this.accountURI = accountURI;
    }

    public void setTransactionURI(String transactionURI) {
        this.transactionURI = transactionURI;
    }

    public void setUserURI(String userURI) {
        this.userURI = userURI;
    }

    public void setDatabaseStructure(String databaseStructure) {
        this.databaseStructure = databaseStructure;
    }
}

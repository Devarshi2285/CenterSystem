package org.example.centralserver.entities.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.example.centralserver.entities.TransectionUser;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("TransectionConfig")
public class TransectionConfig {

    @Id
    private String id;
    private String sender;
    private String receiver;
    private String amt;
    private String type;
    private String currency;
    private String description;
    private String balanceAfterTransection;
    private String createdDate;
    public String getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getAmt() {
        return amt;
    }

    public String getType() {
        return type;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public String getBalanceAfterTransection() {
        return balanceAfterTransection;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBalanceAfterTransection(String balanceAfterTransection) {
        this.balanceAfterTransection = balanceAfterTransection;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}

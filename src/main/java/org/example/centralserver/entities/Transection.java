package org.example.centralserver.entities;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collation = "Transection")
public class Transection implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id; // MongoDB will handle this automatically

    private TransectionUser sender;
    private TransectionUser receiver;
    private double amt;
    private String type;
    private String currency;
    private String description;
    private double balanceAfterTransection;
    private LocalDateTime createdDate;

    // Let Spring Data automatically handle the creation date

    public Transection(String id,TransectionUser sender, TransectionUser receiver, Double amount, String type, String currency,String description , Double balanceAfterTransection,LocalDateTime createdDate) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.amt = amount;
        this.type = type;
        this.currency = currency;
        this.description = description;
        this.balanceAfterTransection = balanceAfterTransection;
        this.createdDate = createdDate;
    }

    public String getId() {
        return id;
    }

    public Object getSender() {
        return sender;
    }

    public Object getReceiver() {
        return receiver;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public double getAmt() {
        return amt;
    }
    // Optionally, you can add a method to handle your own logic for creation date if needed
}

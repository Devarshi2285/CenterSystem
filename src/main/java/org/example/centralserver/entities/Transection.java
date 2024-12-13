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

    private String sender;
    private String receiver;
    private double amt;

    private String senderBank;
    private String receiverBank;
    LocalDateTime createdDate;

    // Let Spring Data automatically handle the creation date

    public Transection(String sender, String receiver, Double amount, String number, String receiverBankId,LocalDateTime createdDate) {
            this.sender = sender;
            this.receiver = receiver;
            this.amt = amount;
            this.senderBank = number;
            this.receiverBank = receiverBankId;
            this.createdDate = createdDate;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    // Optionally, you can add a method to handle your own logic for creation date if needed
}

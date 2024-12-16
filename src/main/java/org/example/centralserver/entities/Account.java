package org.example.centralserver.entities;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
@Getter
@Setter
@Document("accounts")
public class Account implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L; // Add a serialVersionUID for better version control during serialization

    @Id
    private String id;
    private String accId;
    private String bank;
    private String user;
    private double freq=0;//avg transections per day...


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) // Ensures consistent formatting
    private LocalDateTime lastTransaction=null;
    
    private int regularIntervelTransection=0;
    //let's say every day at 2 pm so this will incresee
    private boolean isSuspicious=false;

    private List<String>nominees=new ArrayList<String>();
    public Account(){}
    public Account( String accId, String bank, String user, List<String> nominees) {
        this.accId = accId;
        this.bank = bank;
        this.user = user;
        this.nominees = nominees;
    }

    public void setFreq(double freq) {
        this.freq = freq;
    }

    public void setLastTransaction(LocalDateTime lastTransaction) {
        this.lastTransaction = lastTransaction;
    }

    public double getFreq() {
        return freq;
    }

    public LocalDateTime getLastTransaction() {
        return lastTransaction;
    }
}

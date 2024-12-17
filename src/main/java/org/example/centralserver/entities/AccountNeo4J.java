package org.example.centralserver.entities;




import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
        import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
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
@Node("Account")
public class AccountNeo4J {
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


    @Relationship(type = "SENT", direction = Relationship.Direction.OUTGOING)
    private List<TransectionNeo4J> transactions=new ArrayList<>();

    private List<String> nominees=new ArrayList<String>();
    public AccountNeo4J(){}
    public AccountNeo4J( String accId, String bank, String user, List<String> nominees) {
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

    public List<TransectionNeo4J> getTransactions() {
        return transactions;
    }

    public void addTransactionTo(AccountNeo4J receiver, Transection transaction) {
        // Create the relationship between sender and receiver
        System.out.println("adding edge");
        TransectionNeo4J transectionNeo4J=new TransectionNeo4J(transaction,receiver);
        this.transactions.add(transectionNeo4J);

    }
}

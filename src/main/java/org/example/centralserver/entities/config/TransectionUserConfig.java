package org.example.centralserver.entities.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.example.centralserver.entities.Account;
import org.example.centralserver.entities.User;
import org.springframework.data.mongodb.core.mapping.Document;


@Document("TransectionUserConfig")
public class TransectionUserConfig {

    private String user;
    private String account;
    private String ifsc;
    private String bankName;
    private String branchName;

    public String getUser() {
        return user;
    }

    public String getAccount() {
        return account;
    }

    public String getIfsc() {
        return ifsc;
    }

    public String getBankName() {
        return bankName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setIFSC(String IFSC) {
        this.ifsc = IFSC;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}

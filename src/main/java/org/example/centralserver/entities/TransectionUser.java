package org.example.centralserver.entities;

import org.springframework.beans.factory.annotation.Autowired;

public class TransectionUser {

    private User user;
    private Account account;
    private String ifsc;
    private String bankName;
    private String branchName;

    public TransectionUser() {}

    public TransectionUser(User user, Account account, String IFSC, String BankName, String BranchName) {
        this.user = user;
        this.account = account;
        this.ifsc = IFSC;
        this.bankName = BankName;
        this.branchName = BranchName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getIfsc() {
        return ifsc;
    }

    public void setIfsc(String IFSC) {
        this.ifsc = IFSC;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}

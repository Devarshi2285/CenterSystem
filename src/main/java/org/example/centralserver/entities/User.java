package org.example.centralserver.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
public class User {


    @Id
    private String id;
    private String name;
    private String govIdNum;
    private String email;
    private String mobileNumber;
    private String idType;
    private String address;
    private List<String> accounts = new ArrayList<>();

    public User(String name, String govIdNum, String email, String mobileNumber, String idType, String address, List<String> accounts) {
        this.name = name;
        this.govIdNum = govIdNum;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.idType = idType;
        this.address = address;
        this.accounts = accounts;
    }

    public User() {

    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", govIdNum='" + govIdNum + '\'' +
                ", email='" + email + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", idType='" + idType + '\'' +
                ", address='" + address + '\'' +
                ", accounts=" + accounts +
                '}';
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getGovIdNum() { return govIdNum; }
    public String getEmail() { return email; }
    public String getMobileNumber() { return mobileNumber; }
    public String getIdType() { return idType; }
    public String getAddress() { return address; }
    public List<String> getAccounts() { return accounts; }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGovIdNum(String govIdNum) {
        this.govIdNum = govIdNum;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAccounts(List<String> accounts) {
        this.accounts = accounts;
    }
}
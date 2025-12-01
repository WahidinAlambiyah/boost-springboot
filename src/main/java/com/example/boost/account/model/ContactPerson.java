package com.example.boost.account.model;

import jakarta.persistence.*;

@Entity
@Table(name = "contact_persons")
public class ContactPerson {
    @Id
    private String id;

    private String name;
    private String email;
    private String phone;

    @ManyToOne
    private Account account;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}

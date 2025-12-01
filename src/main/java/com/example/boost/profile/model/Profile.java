package com.example.boost.profile.model;

import com.example.boost.account.model.Account;
import com.example.boost.user.model.User;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "profiles")
public class Profile {
    @Id
    private String id;

    @OneToOne(optional = false)
    private User user;

    @ManyToOne
    private Account account;

    private String name;
    private String imageUrl;
    private String passport;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getPassport() { return passport; }
    public void setPassport(String passport) { this.passport = passport; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}

package com.ecommerce.member.memberEntity;


import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true)
    public String email;

    public String password;

    public String salt;

    @Column(name = "user_name")
    public String userName;

    @Column(name = "phone_number", unique = true)
    public String phoneNumber;

    public String role;

    public UserEntity() {
    }

    public UserEntity(String email, String userName, String password, String salt, String phoneNumber, String role) {
        if(email == null    || email.isBlank())    throw new IllegalArgumentException("Email is necessary");
        if(password == null || password.isBlank()) throw new IllegalArgumentException("Password is necessary");

        this.email = email;
        this.userName = userName;
        this.password = password;
        this.salt = salt;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

}

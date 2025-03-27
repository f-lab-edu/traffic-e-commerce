package com.ecommerce.member.memberEntity;


import jakarta.persistence.*;

@Entity
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true)
    public String email;

    public String userName;

    public String password;

    public String salt;

    @Column(unique = true)
    public String phoneNumber;

    public UserEntity(String email, String userName, String password, String salt, String phoneNumber, String role) {
        this.email = email;
        this.userName = userName;
        this.password = password;
        this.salt = salt;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public String role;

    public UserEntity() {
    }




}

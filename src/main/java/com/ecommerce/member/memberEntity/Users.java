package com.ecommerce.member.memberEntity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    @Id
    @Column(name = "user_id", nullable = false)
    public String userId;

    @Column(unique = true)
    public String email;

    public String password;

    public String salt;

    @Column(name = "user_name")
    public String userName;

    @Column(name = "phone_number", unique = true)
    public String phoneNumber;

    public String role;

    public static Users of(String userId, String email, String password, String salt, String userName, String phoneNumber, String role) {
        return Users.builder()
                .userId(userId).email(email).password(password).salt(salt).userName(userName).phoneNumber(phoneNumber).role(role)
                .build();
    }

}

package com.ecommerce.member.dto.requset;

public class RegisterRequest {

    public String email;

    public String userName;

    public String phoneNumber;

    public String role;

    public String password;

    public boolean hasNotPassword() {
        return password == null || password.isBlank();
    }

    public RegisterRequest(String email, String userName, String phoneNumber, String role, String password) {
        this.email = email;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.password = password;
    }
}

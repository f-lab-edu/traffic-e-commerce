package com.ecommerce.member.dto.requset;

public class ModifyUserRequest {

    public String userName;

    public String phoneNumber;

    public ModifyUserRequest(String userName, String phoneNumber) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
    }
}

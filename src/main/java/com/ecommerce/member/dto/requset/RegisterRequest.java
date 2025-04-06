package com.ecommerce.member.dto.requset;

import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    public String email;

    public String userName;

    public String phoneNumber;

    public String role;

    public String password;

    public boolean hasNotPassword() {
        return StringUtils.isBlank(password) || StringUtils.isEmpty(password);
    }

}

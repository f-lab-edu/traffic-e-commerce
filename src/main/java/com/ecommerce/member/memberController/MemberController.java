package com.ecommerce.member.memberController;

import com.ecommerce.member.dto.requset.LoginRequest;
import com.ecommerce.member.dto.requset.ModifyUserRequest;
import com.ecommerce.member.dto.requset.RegisterRequest;
import com.ecommerce.member.dto.response.LoginResponse;
import com.ecommerce.member.memberEntity.Users;
import com.ecommerce.member.memberService.UserService;
import com.ecommerce.util.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/member/users/registration")
    public Users register(@RequestBody RegisterRequest registerRequest) throws NoSuchAlgorithmException {
        return userService.registerUser(registerRequest);
    }

    @PostMapping("/member/users/modify")
    public Users modifyUser(@RequestBody ModifyUserRequest dto, @RequestHeader("Authorization") String token) {
        String email = jwtService.generateToken(token);
        return userService.modifyUser(email, dto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Validated @RequestBody LoginRequest loginRequest) throws NoSuchAlgorithmException {
        LoginResponse response = userService.responseToken(loginRequest);
        return ResponseEntity.ok(response);
    }

}

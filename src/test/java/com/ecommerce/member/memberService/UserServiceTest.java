package com.ecommerce.member.memberService;

import com.ecommerce.member.dto.requset.LoginRequest;
import com.ecommerce.member.dto.requset.ModifyUserRequest;
import com.ecommerce.member.dto.requset.RegisterRequest;
import com.ecommerce.member.dto.response.LoginResponse;
import com.ecommerce.member.memberEntity.Users;
import com.ecommerce.util.JsonUtilsDeco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.BDDAssertions.then;

@SpringBootTest
class UserServiceTest {

    @Autowired private UserService sut;


    @Test
    @DisplayName("빈이 Null은 아닌지")
    void isBeanNotEmpty() {
        then(sut).isNotNull();
    }


    @Test
    @DisplayName("회원가입 테스트")
    void registerTest() throws NoSuchAlgorithmException {

        // Given
        var registerRequest = new RegisterRequest("abcd@mail.com", "tempuser", "010-0000-0000", "CUSTOMER", "passwordTest");

        // When
        Users userEntity = sut.registerUser(registerRequest);

        // Then
        then(userEntity).isNotNull();

    }


    @Test
    @DisplayName("로그인 요청시 JWT 토큰생성과 응답 테스트")
    void responseTokenTest() throws NoSuchAlgorithmException {

        // Given
        LoginRequest passwordTest = new LoginRequest("abcd@mail.com", "passwordTest");

        // When
        LoginResponse loginResponse = sut.responseToken(passwordTest);

        // Then
        then(loginResponse).isNotNull();

        JsonUtilsDeco.prettyGson.toJson(loginResponse);
    }

    @Test
    @DisplayName("유저 정보 변경 테스트")
    void modifyUserTest() {
        String email = "abcd@mail.com";
        ModifyUserRequest userRequest = new ModifyUserRequest("tempuser", "010-0000-1234");
        sut.modifyUser(email, userRequest);
    }

}


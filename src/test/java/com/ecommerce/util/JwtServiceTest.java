package com.ecommerce.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.BDDAssertions.then;

@Slf4j
@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void isBeanNotNull() {
        then(jwtService).isNotNull();
    }


    @Test
    @DisplayName("토큰 발행 테스트")
    void generateTokenTest() {

        // Given
        // String userId = "3210dcc2-2d85-455c-baa2-a062bccce636";
         String userId = "bdd36fb4-02a9-4ef0-8596-db61cc6d6c9a";

        // When
        String token = jwtService.generateToken(userId);

        // Then
        then(token).isNotNull();

        log.info("token >> {}", token);

        // String userId = "3210dcc2-2d85-455c-baa2-a062bccce636";
        // token >> eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzMjEwZGNjMi0yZDg1LTQ1NWMtYmFhMi1hMDYyYmNjY2U2MzYiLCJpYXQiOjE3NDQwMTgwMTAsImV4cCI6MTc0NDAyMTYxMH0._nf1hQRPFtPolI4asBuQQmHPu--vw1vLBYU4wg8FTHc

        // String userId = "bdd36fb4-02a9-4ef0-8596-db61cc6d6c9a";
        // token >> eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJiZGQzNmZiNC0wMmE5LTRlZjAtODU5Ni1kYjYxY2M2ZDZjOWEiLCJpYXQiOjE3NDQwMTgxOTksImV4cCI6MTc0NDAyMTc5OX0.q1HyqhyhQkcaXieAua1JJSyajc5mnAA_o9x7TIxCxNw

    }

    @Test
    void extractUserId() {

        // Given
        String userId = "3210dcc2-2d85-455c-baa2-a062bccce636";

        // When
        String token = jwtService.generateToken(userId);
        String extractEmail = jwtService.extractUserId(token);

        // Then
        then(extractEmail).isNotNull();
        log.info("extractUserId >> {}", extractEmail);
    }
}

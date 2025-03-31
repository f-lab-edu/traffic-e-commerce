package com.ecommerce.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.BDDAssertions.then;

@Slf4j
@SpringBootTest
class JwtUtilTest {

    @Autowired private JwtUtil jwtUtil;

    @Test
    void isBeanNotNull() {
        then(jwtUtil).isNotNull();
    }


    @Test
    @DisplayName("토큰 발행 테스트")
    void generateTokenTest() {

        // Given
        String email = "abcd@mail.com";

        // When
        String token = jwtUtil.generateToken(email);

        // Then
        then(token).isNotNull();

        log.info("token >> {}", token);
    }

    @Test
    void extractEmail() {
        // Given
        String email = "abcd@mail.com";

        // When
        String token = jwtUtil.generateToken(email);
        String extractEmail = jwtUtil.extractEmail(token);

        // Then
        then(extractEmail).isNotNull();
        log.info("extractEmail >> {}", extractEmail);
    }
}

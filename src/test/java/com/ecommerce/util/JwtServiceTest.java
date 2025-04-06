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

    @Autowired private JwtService jwtService;

    @Test
    void isBeanNotNull() {
        then(jwtService).isNotNull();
    }


    @Test
    @DisplayName("토큰 발행 테스트")
    void generateTokenTest() {

        // Given
        String userId = "e7a96106-c4bb-457e-9e86-c859e93b21a9";

        // When
        String token = jwtService.generateToken(userId);

        // Then
        then(token).isNotNull();

        log.info("token >> {}", token);
    }

    @Test
    void extractUserId() {

        // Given
        String userId = "e7a96106-c4bb-457e-9e86-c859e93b21a9";

        // When
        String token = jwtService.generateToken(userId);
        String extractEmail = jwtService.extractUserId(token);

        // Then
        then(extractEmail).isNotNull();
        log.info("extractUserId >> {}", extractEmail);
    }
}

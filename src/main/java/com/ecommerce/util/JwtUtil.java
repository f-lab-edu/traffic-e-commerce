package com.ecommerce.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtKeyProvider keyProvider;

    public String generateToken(String id) {

        return Jwts.builder()
                .setSubject(id)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(keyProvider.getKey(), Jwts.SIG.HS256)
                .compact();

    }

    public String extractEmail(String token) {
        Jws<Claims> claimsJws = Jwts.parser().verifyWith(keyProvider.getKey()).build().parseSignedClaims(token);
        return replacePrefix(claimsJws.getPayload().getSubject());
    }

    private String replacePrefix(String subject) {
        return subject.replace("Bearer ", "");
    }

}

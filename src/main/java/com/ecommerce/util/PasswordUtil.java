package com.ecommerce.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashWithSalt(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest instance = MessageDigest.getInstance("SHA-256");

        byte[] saltByte = (password + salt).getBytes(StandardCharsets.UTF_8);
        instance.update(saltByte);

        return Base64.getEncoder().encodeToString(instance.digest());
    }


}

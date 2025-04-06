package com.ecommerce.member.memberService;

import com.ecommerce.member.dto.requset.LoginRequest;
import com.ecommerce.member.dto.requset.ModifyUserRequest;
import com.ecommerce.member.dto.requset.RegisterRequest;
import com.ecommerce.member.dto.response.LoginResponse;
import com.ecommerce.member.memberEntity.Users;
import com.ecommerce.member.memberRepository.UserRepository;
import com.ecommerce.util.JwtService;
import com.ecommerce.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public Users registerUser(RegisterRequest registerRequest) throws NoSuchAlgorithmException {
        if (registerRequest.hasNotPassword()) {
            throw new IllegalArgumentException("Entering password is necessary");
        }

        String salt = PasswordUtil.generateSalt();
        String hashWithSalt = PasswordUtil.hashWithSalt(registerRequest.password, salt);
        String userId = UUID.randomUUID().toString();

        var user = Users.of(userId, registerRequest.email, hashWithSalt, salt, registerRequest.userName, registerRequest.phoneNumber, registerRequest.role);

        return userRepository.save(user);
    }

    public Users modifyUser(String email, ModifyUserRequest dto) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.userName = dto.userName;
        user.phoneNumber = dto.phoneNumber;

        return userRepository.save(user);
    }


    public LoginResponse responseToken(LoginRequest loginRequest) throws NoSuchAlgorithmException {
        Users loginUser = checkValidPasswd(loginRequest);
        String token = jwtService.generateToken(loginUser.userId);

        return new LoginResponse(token);
    }

    public Users checkValidPasswd(LoginRequest loginRequest) throws NoSuchAlgorithmException {
        Users user = getUserByEmail(loginRequest);
        String hashWithSalt = PasswordUtil.hashWithSalt(loginRequest.password, user.salt);

        if (!hashWithSalt.equals(user.password)) {
            throw new IllegalArgumentException("Not valid password");
        }

        return user;
    }

    private Users getUserByEmail(LoginRequest loginRequest) {
        return userRepository.findByEmail(loginRequest.email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }


}

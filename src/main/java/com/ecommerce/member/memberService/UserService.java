package com.ecommerce.member.memberService;

import com.ecommerce.member.dto.requset.LoginRequest;
import com.ecommerce.member.dto.requset.ModifyUserRequest;
import com.ecommerce.member.dto.requset.RegisterRequest;
import com.ecommerce.member.dto.response.LoginResponse;
import com.ecommerce.member.memberEntity.UserEntity;
import com.ecommerce.member.memberRepository.UserRepository;
import com.ecommerce.util.JwtUtil;
import com.ecommerce.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public UserEntity registerUser(RegisterRequest registerRequest) throws NoSuchAlgorithmException {
        if (registerRequest.hasNotPassword()) {
            throw new IllegalArgumentException("Entering password is necessary");
        }

        String salt = PasswordUtil.generateSalt();
        String hashWithSalt = PasswordUtil.hashWithSalt(registerRequest.password, salt);

        var user = new UserEntity(registerRequest.email, registerRequest.userName, hashWithSalt, salt, registerRequest.phoneNumber, registerRequest.role);

        return userRepository.save(user);
    }

    public UserEntity modifyUser(String email, ModifyUserRequest dto) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.userName = dto.userName;
        user.phoneNumber = dto.phoneNumber;

        return userRepository.save(user);
    }


    public LoginResponse responseToken(LoginRequest loginRequest) throws NoSuchAlgorithmException {
        UserEntity loginUser = checkValidPasswd(loginRequest);
        String token = jwtUtil.generateToken(loginUser.email);

        return new LoginResponse(token);
    }

    public UserEntity checkValidPasswd(LoginRequest loginRequest) throws NoSuchAlgorithmException {
        UserEntity user = getUserByEmail(loginRequest);
        String hashWithSalt = PasswordUtil.hashWithSalt(loginRequest.password, user.salt);

        if (!hashWithSalt.equals(user.password)) {
            throw new IllegalArgumentException("Not valid password");
        }

        return user;
    }

    private UserEntity getUserByEmail(LoginRequest loginRequest) {
        return userRepository.findByEmail(loginRequest.email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }


}

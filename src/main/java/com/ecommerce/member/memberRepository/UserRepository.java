package com.ecommerce.member.memberRepository;

import com.ecommerce.member.memberEntity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);

    Optional<Users> findByUserId(UUID userId);

}

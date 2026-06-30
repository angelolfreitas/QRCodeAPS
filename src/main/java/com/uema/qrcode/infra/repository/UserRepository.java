package com.uema.qrcode.infra.repository;

import com.uema.qrcode.entity.definition.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String login);
}
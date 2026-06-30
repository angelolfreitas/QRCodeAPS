package com.uema.qrcode.infra.repository;

import com.uema.qrcode.entity.definition.Registry;
import com.uema.qrcode.entity.definition.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String login);
    @Query("SELECT r FROM User u JOIN u.registry r")
    List<Registry> findAllRegistriesGlobal();
}
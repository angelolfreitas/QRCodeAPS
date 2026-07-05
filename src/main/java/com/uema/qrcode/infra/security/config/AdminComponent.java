package com.uema.qrcode.infra.security.config;
import com.uema.qrcode.entity.definition.role.Role;
import com.uema.qrcode.entity.dto.user.RegisterRequest;
import com.uema.qrcode.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminComponent {
    @Value("${spring.security.user.name}")
    private String adminEmail;
    @Value("${spring.security.user.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initAdmin(AuthenticationService authentication) {
        return args ->
                authentication.register(
                        RegisterRequest.noRole("admin", adminEmail, adminPassword, "", ""),
                        Role.ADMIN
                );

    }
}

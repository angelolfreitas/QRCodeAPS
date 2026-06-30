package com.uema.qrcode.controller.auth;

import com.uema.qrcode.entity.definition.role.Role;
import com.uema.qrcode.entity.dto.user.LoginRequest;
import com.uema.qrcode.entity.dto.user.LoginResponse;
import com.uema.qrcode.entity.dto.user.RegisterRequest;
import com.uema.qrcode.entity.dto.user.RegisterResponse;
import com.uema.qrcode.service.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthenticationController {
    AuthenticationService authenticationService;
    @PostMapping("/sign-up")
    ResponseEntity<RegisterResponse> signUp(@RequestBody RegisterRequest registerRequest) {
        Optional<RegisterResponse> registerResponse = authenticationService.register(registerRequest,
                Role.USER);
        return registerResponse.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
    @PostMapping("/sign-in")
    ResponseEntity<LoginResponse> signIn(@RequestBody LoginRequest loginRequest) {
        Optional<LoginResponse> loginResponse = authenticationService.login(loginRequest);
        return loginResponse.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}

package com.uema.qrcode.service;
import com.uema.qrcode.entity.definition.User;
import com.uema.qrcode.entity.definition.role.Role;
import com.uema.qrcode.entity.dto.user.LoginRequest;
import com.uema.qrcode.entity.dto.user.LoginResponse;
import com.uema.qrcode.entity.dto.user.RegisterRequest;
import com.uema.qrcode.entity.dto.user.RegisterResponse;
import com.uema.qrcode.infra.repository.UserRepository;
import com.uema.qrcode.infra.security.TokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public Optional<LoginResponse> login(LoginRequest login) {
        User user = this.userRepository.findByEmail(login.email())
                .orElseThrow(
                        ()->new RuntimeException(login.email()
                        ));
        if(!passwordEncoder.matches(login.password(), user.getPassword()))
            throw new RuntimeException("senha1 "+login.password()+" senha 2 "+user.getPassword());

        String token = tokenService.generateToken(user);

        return Optional.of(new LoginResponse(login.email(),token, user.getRole().toString()));
    }
    public Optional<RegisterResponse> register(RegisterRequest register, Role role) {
        Optional<User> verifyUser = this.userRepository.findByEmail(register.email());
        if(verifyUser.isPresent())
            throw new RuntimeException(register.email());
        return Optional.of(buildUser(register, role));
    }

    private RegisterResponse buildUser(RegisterRequest register, Role role) {
        User user = User.builder()
                .password(passwordEncoder.encode(register.password()))
                .email(register.email())
                .username(register.name())
                .role(role)
                .build();

        this.userRepository.save(user);

        String token = tokenService.generateToken(user);
        return new RegisterResponse(user.getUsername(),token);
    }

    public Optional<RegisterResponse> registerManager(RegisterRequest register) {
        Optional<User> verifyUser = this.userRepository.findByEmail(register.email());
        //usuario inexistente, criado como manager
        if(verifyUser.isEmpty())
            return Optional.of(buildUser(register, Role.MANAGER));
        //usuário existente, atualiza a role
        User user = verifyUser.get();
        //retorna nada se o usuário já é manager
        if(!updateRole(user, Role.MANAGER))
            throw new RuntimeException(register.name());


        String newToken = tokenService.generateToken(user);
        return Optional.of(new RegisterResponse(user.getUsername(), newToken));
    }

    public boolean updateRole(User user, Role newRole) {
        if (user.getRole() == newRole)
            return false;

        user.setRole(newRole);
        this.userRepository.save(user);
        return true;
    }


    @Transactional
    public RegisterResponse updateUser(RegisterRequest projectDTO, User user) {
        user.setEmail(projectDTO.email());
        user.setPassword(passwordEncoder.encode(projectDTO.password()));
        user.setUsername(projectDTO.name());

        this.userRepository.save(user);
        String newToken = tokenService.generateToken(user);
        return new RegisterResponse(user.getUsername(), newToken);
    }
    @Transactional
    public void patchUser(User user, Map<String, Object> updates) {
        updates.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(User.class, key);
            field.setAccessible(true);
            ReflectionUtils.setField(field, user, value);
        });
        this.userRepository.save(user);
    }
}


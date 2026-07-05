package com.uema.qrcode.controller.cadastro;

import com.uema.qrcode.entity.definition.User;
import com.uema.qrcode.entity.definition.role.Role;
import com.uema.qrcode.entity.dto.user.ClienteOptionResponse;
import com.uema.qrcode.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<ClienteOptionResponse>> listar() {
        Optional<List<User>> listaOpt = userRepository.findByRole(Role.USER);
        if (listaOpt.isEmpty()) {return ResponseEntity.notFound().build();}
        List<User> lista = listaOpt.get();
        return ResponseEntity.ok(lista.stream()
                .map(u -> new ClienteOptionResponse(u.getId(), u.getUsername(), u.getEmail()))
                .toList());
    }
}
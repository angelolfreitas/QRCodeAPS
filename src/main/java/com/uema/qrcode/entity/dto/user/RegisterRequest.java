package com.uema.qrcode.entity.dto.user;

import com.uema.qrcode.entity.definition.role.Role;

public record RegisterRequest(String name, String email, String password, String equipe, String crea, Role role) {
    public static RegisterRequest noRole(String name, String email, String password, String equipe, String crea){
        return new RegisterRequest(name, email, password, equipe, crea, Role.USER);
    }
}

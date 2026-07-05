package com.uema.qrcode.entity.dto.ponto;

public record RegisterRequest(
        String codigo,
        String localizacao,
        String descricao,
        String criticidade,
        String status,
        String clienteId,
        String areaId
) {
}

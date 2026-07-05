package com.uema.qrcode.entity.dto.ponto;

public record RegisterResponse(
        String id,
        String codigo,
        String localizacao,
        String descricao,
        String criticidade,
        String status,
        String qrCodeUrl,
        String clienteId,
        String areaId,
        String tipoPontoId
) {
}

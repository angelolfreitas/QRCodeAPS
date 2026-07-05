package com.uema.qrcode.entity.dto.ponto;

public record InspecaoRequest(
        String responsavel,
        Double resistenciaAterramento,
        Integer continuidadeEletrica,
        String condicaoVisual,
        Boolean possuiOxidacao,
        Boolean necessitaCorrecao,
        Boolean conforme,
        String observacoes
) {
}
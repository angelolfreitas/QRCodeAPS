package com.uema.qrcode.entity.dto.inspecao;

import java.util.List;

public record InspecaoResponse(
        String codigo,
        String cliente,
        String area,
        String tipo,
        String localizacao,
        String descricao,
        String criticidade,
        String status,
        List<InspecaoHistoricoItem> historico
) {
    public record InspecaoHistoricoItem(
            String dataInspecao,
            String responsavel,
            Boolean conforme,
            Double resistenciaAterramento,
            Integer continuidadeEletrica,
            String condicaoVisual,
            String observacoes,
            String pdfUrl
    ) {}
}
package com.uema.qrcode.entity.definition;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inspecoes")
public class Inspecao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "ponto_id", nullable = false)
    private String pontoId;

    @Column(name = "inspetor_id", nullable = false)
    private String inspetorId;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "data_inspecao")
    private LocalDateTime dataInspecao;

    private String responsavel;
    private Double resistenciaAterramento;
    private Integer continuidadeEletrica;
    private String condicaoVisual;
    private Boolean possuiOxidacao;
    private Boolean necessitaCorrecao;
    private Boolean conforme;

    @Column(length = 1000)
    private String observacoes;
}
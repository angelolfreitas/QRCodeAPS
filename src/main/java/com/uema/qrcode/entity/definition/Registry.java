package com.uema.qrcode.entity.definition;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Registry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario; // O ID do usuário que a provocou

    @Column(nullable = false, length = 150)
    private String acao;

    @Column(name = "link_aws_s3", nullable = false, length = 512)
    private String linkAwsS3; // O atributo link enviado para a AWS que resulta no QR Code

    @Column(name = "data_registro", updatable = false)
    private LocalDateTime dataRegistro = LocalDateTime.now();
}

package com.uema.qrcode.entity.definition;

import com.uema.qrcode.entity.definition.role.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "points")
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private String id;

    // Código visível do QR Code (ex: TQ-001)
    @Column(unique = true)
    private String codigo;

    // Campos diretos do seu formulário frontend
    private String localizacao;
    private String descricao;
    private String criticidade;
    private String status;

    @Column(name = "qr_code_url")
    private String qrCodeUrl;

    /* * RELACIONAMENTOS SIMPLIFICADOS
     * O frontend manda os IDs. Você pode salvar os IDs diretamente como String
     * ou criar as classes Cliente, Area e Tipo depois e usar @ManyToOne
     */
    @Column(name = "cliente_id")
    private String clienteId;

    @Column(name = "area_id")
    private String areaId;

    @Column(name = "tipo_ponto_id")
    private String tipoPontoId;

    @Column(name = "ultimo_pdf_url")
    private String ultimoPdfUrl;
}

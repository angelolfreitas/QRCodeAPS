package com.uema.qrcode.service;

import com.uema.qrcode.entity.definition.Inspecao;
import com.uema.qrcode.entity.definition.Point;
import com.uema.qrcode.entity.definition.User;
import com.uema.qrcode.entity.definition.role.Role;
import com.uema.qrcode.entity.dto.inspecao.InspecaoRequest;
import com.uema.qrcode.entity.dto.qr.QRCodeResponse;
import com.uema.qrcode.infra.port.StoragePort;
import com.uema.qrcode.infra.repository.InspecaoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InspecaoService {

    private final PointService pointService;
    private final QRCodeService qrCodeService;
    private final RegistryService registryService;
    private final StoragePort storagePort;
    private final InspecaoRepository inspecaoRepository;

    @Transactional
    public QRCodeResponse registrarInspecao(String codigo, InspecaoRequest dados, MultipartFile arquivoPdf, User inspetor) throws Exception {
        Point ponto = pointService.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException(codigo));

        verificarPermissao(ponto, inspetor);

        String urlPdf = subirPdf(codigo, arquivoPdf);

        Inspecao inspecao = Inspecao.builder()
                .pontoId(ponto.getId())
                .inspetorId(inspetor.getId())
                .pdfUrl(urlPdf)
                .dataInspecao(LocalDateTime.now())
                .responsavel(dados.responsavel())
                .resistenciaAterramento(dados.resistenciaAterramento())
                .continuidadeEletrica(dados.continuidadeEletrica())
                .condicaoVisual(dados.condicaoVisual())
                .possuiOxidacao(dados.possuiOxidacao())
                .necessitaCorrecao(dados.necessitaCorrecao())
                .conforme(dados.conforme())
                .observacoes(dados.observacoes())
                .build();
        inspecaoRepository.save(inspecao);

        String novoQrDoPonto = qrCodeService.uploadQRCode(urlPdf);
        pointService.atualizarAposInspecao(ponto, urlPdf, novoQrDoPonto);

        return registryService.registrar(urlPdf, inspetor, "0");
    }

    private void verificarPermissao(Point ponto, User usuario) {
        System.out.println("Responsavel no Ponto: [" + ponto.getResponsavelId() + "] Tipo: " + (ponto.getResponsavelId() != null ? ponto.getResponsavelId().getClass().getSimpleName() : "NULO"));
        System.out.println("ID do Usuario Logado: [" + usuario.getId() + "] Tipo: " + (usuario.getId() != null ? usuario.getId().getClass().getSimpleName() : "NULO"));
        
        boolean isAdminOuManager = usuario.getRole() == Role.ADMIN || usuario.getRole() == Role.MANAGER;
        boolean isResponsavel = ponto.getResponsavelId() != null && ponto.getResponsavelId().equals(usuario.getId());

        if (!isAdminOuManager && !isResponsavel) {
            throw new RuntimeException("Você não tem permissão para alterar este ponto.");
        }
    }

    private String subirPdf(String codigo, MultipartFile arquivoPdf) throws Exception {
        byte[] pdfBytes = arquivoPdf.getBytes();
        String nomeArquivo = "inspecoes/laudo-" + codigo + "-" + UUID.randomUUID() + ".pdf";
        return storagePort.uploadFile(pdfBytes, nomeArquivo, "application/pdf").split("\\?")[0];
    }
}
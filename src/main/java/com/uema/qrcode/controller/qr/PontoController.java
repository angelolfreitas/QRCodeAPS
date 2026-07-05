package com.uema.qrcode.controller.qr;

import com.uema.qrcode.entity.definition.Point;
import com.uema.qrcode.entity.definition.User;
import com.uema.qrcode.entity.definition.role.Role;
import com.uema.qrcode.entity.dto.ponto.InspecaoRequest;
import com.uema.qrcode.entity.dto.ponto.RegisterRequest;
import com.uema.qrcode.entity.dto.ponto.RegisterResponse;
import com.uema.qrcode.entity.dto.qr.QRCodeResponse;
import com.uema.qrcode.infra.port.StoragePort;
import com.uema.qrcode.service.PointService;
import com.uema.qrcode.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/pontos")
@RequiredArgsConstructor
public class PontoController {
    private final PointService pointService;
    private final QRCodeService qrCodeService; // Injetando seu serviço de QR Code
    private final StoragePort storagePort;     // Injetando a porta da AWS
    @org.springframework.beans.factory.annotation.Value("${app.base-url}")
    private String baseUrl;


    @PostMapping(value = "/codigo/{codigo}/inspecoes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> salvarInspecao(
            @PathVariable String codigo,
            @RequestPart("dadosInspecao") InspecaoRequest request,
            @RequestPart("arquivoPdf") MultipartFile arquivoPdf,
            @AuthenticationPrincipal User usuarioLogado
    ) {
        Optional<Point> pontoOpt = pointService.findByCodigo(codigo);
        if (pontoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Point ponto = pontoOpt.get();

        try {
            // 1. Upload do PDF pro S3
            byte[] pdfBytes = arquivoPdf.getBytes();
            String nomeArquivoPdf = "inspecoes/laudo-" + codigo + "-" + UUID.randomUUID() + ".pdf";
            String urlDoPdfNaAws = storagePort.uploadFile(pdfBytes, nomeArquivoPdf, "application/pdf");
            String urlLimpaParaOQrCode = urlDoPdfNaAws.split("\\?")[0];

            // 2. Atualiza o PONTO (o que o admin vê no dashboard)
            ponto.setUltimoPdfUrl(urlLimpaParaOQrCode);
            ponto.setStatus("INSPECIONADO");
            String novoQrDoPonto = qrCodeService.uploadQRCode(urlLimpaParaOQrCode);
            ponto.setQrCodeUrl(novoQrDoPonto);
            pointService.updateStatusPdf(ponto);

            // 3. (Opcional) mantém histórico no Registry do inspetor também
            QRCodeResponse qrCodeGerado = qrCodeService.registrateAndGenerateQRCode(
                    urlLimpaParaOQrCode,
                    usuarioLogado,
                    0
            );

            return ResponseEntity.ok(qrCodeGerado);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro ao gerar PDF ou QR Code na AWS.");
        }
    }
    @GetMapping
    public ResponseEntity<List<Point>> listarPontos() {
        return ResponseEntity.ok(pointService.points().orElse(List.of()));
    }

    @PostMapping
    public ResponseEntity<?> cadastrarPonto(
            @RequestBody RegisterRequest request,
            @AuthenticationPrincipal User usuarioLogado
    ) {
        if (usuarioLogado.getRole() != Role.ADMIN && usuarioLogado.getRole() != Role.MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Apenas administradores e gestores podem cadastrar pontos.");
        }
        try {
            return ResponseEntity.ok(pointService.cadastrarPonto(request));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro ao cadastrar ponto e gerar QR Code.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerPonto(
            @PathVariable String id,
            @AuthenticationPrincipal User usuarioLogado
    ) {
        if (usuarioLogado.getRole() != Role.ADMIN && usuarioLogado.getRole() != Role.MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Apenas administradores e gestores podem remover pontos.");
        }
        try {
            pointService.deletePoint(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/codigo/{codigo}/redirect")
    public ResponseEntity<Void> redirecionarParaUltimoLaudo(@PathVariable String codigo) {
        Point ponto = pointService.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Ponto não encontrado"));

        String destino = (ponto.getUltimoPdfUrl() != null)
                ? ponto.getUltimoPdfUrl()
                : baseUrl + "/site/user/inspecao.html?codigo=" + codigo;

        return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                .location(java.net.URI.create(destino))
                .build();
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<?> buscarPontoParaInspecao(@PathVariable String codigo) {
        return pointService.findByCodigo(codigo)
                .map(ponto -> ResponseEntity.ok(Map.of(
                        "codigo", ponto.getCodigo(),
                        "cliente", ponto.getClienteId() != null ? ponto.getClienteId() : "-",
                        "area", ponto.getAreaId() != null ? ponto.getAreaId() : "-",
                        "tipo", ponto.getTipoPontoId() != null ? ponto.getTipoPontoId() : "-",
                        "localizacao", ponto.getLocalizacao() != null ? ponto.getLocalizacao() : "-",
                        "descricao", ponto.getDescricao() != null ? ponto.getDescricao() : "-",
                        "criticidade", ponto.getCriticidade() != null ? ponto.getCriticidade() : "-",
                        "status", ponto.getStatus() != null ? ponto.getStatus() : "-",
                        "historico", List.of()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}

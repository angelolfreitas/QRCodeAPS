package com.uema.qrcode.controller.qr;

import com.uema.qrcode.entity.definition.Point;
import com.uema.qrcode.entity.definition.User;
import com.uema.qrcode.entity.dto.inspecao.InspecaoRequest;
import com.uema.qrcode.entity.dto.inspecao.InspecaoResponse;
import com.uema.qrcode.entity.dto.ponto.RegisterRequest;
import com.uema.qrcode.entity.dto.qr.QRCodeResponse;
import com.uema.qrcode.service.InspecaoService;
import com.uema.qrcode.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pontos")
@RequiredArgsConstructor
public class PontoController {

    private final PointService pointService;
    private final InspecaoService inspecaoService;

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping
    public ResponseEntity<List<Point>> listarPontos() {
        return ResponseEntity.ok(pointService.points().orElse(List.of()));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<InspecaoResponse> buscarPontoParaInspecao(@PathVariable String codigo) {
        return pointService.getByCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('manager::write')")
    public ResponseEntity<?> cadastrarPonto(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(pointService.cadastrarPonto(request));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro ao cadastrar ponto e gerar QR Code.");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('manager::delete')")
    public ResponseEntity<?> removerPonto(@PathVariable String id) {
        try {
            pointService.deletePoint(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/codigo/{codigo}/inspecoes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('user::write')")
    public ResponseEntity<?> salvarInspecao(
            @PathVariable String codigo,
            @RequestPart("dadosInspecao") InspecaoRequest dadosInspecao,
            @RequestPart("arquivoPdf") MultipartFile arquivoPdf,
            @AuthenticationPrincipal User usuarioLogado
    ) {
        try {
            QRCodeResponse resultado = inspecaoService.registrarInspecao(codigo, dadosInspecao, arquivoPdf, usuarioLogado);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro ao gerar PDF ou QR Code na AWS.");
        }
    }

    @GetMapping("/codigo/{codigo}/redirect")
    public ResponseEntity<Void> redirecionarParaUltimoLaudo(@PathVariable String codigo) {
        Point ponto = pointService.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException(codigo));

        String destino = (ponto.getUltimoPdfUrl() != null)
                ? ponto.getUltimoPdfUrl()
                : baseUrl + "/site/user/inspecao.html?codigo=" + codigo;

        return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                .location(URI.create(destino))
                .build();
    }
}
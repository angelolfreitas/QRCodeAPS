package com.uema.qrcode.service;

import com.uema.qrcode.entity.definition.Point;
import com.uema.qrcode.entity.dto.inspecao.InspecaoResponse;
import com.uema.qrcode.entity.dto.ponto.RegisterRequest;
import com.uema.qrcode.entity.dto.ponto.RegisterResponse;
import com.uema.qrcode.infra.repository.InspecaoRepository;
import com.uema.qrcode.infra.repository.PointRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PointService {

    private final PointRepository pointRepository;
    private final QRCodeService qrCodeService;

    @Value("${app.base-url}")
    private String baseUrl;

    private final InspecaoRepository inspecaoRepository; // adicionar junto dos outros campos "private final"

    public Optional<InspecaoResponse> getByCodigo(String codigo) {
        Optional<Point> pointOpt = findByCodigo(codigo);
        if (pointOpt.isEmpty()) return Optional.empty();
        Point point = pointOpt.get();

        List<InspecaoResponse.InspecaoHistoricoItem> historico = inspecaoRepository
                .findByPontoIdOrderByDataInspecaoDesc(point.getId())
                .stream()
                .map(i -> new InspecaoResponse.InspecaoHistoricoItem(
                        i.getDataInspecao().toString(),
                        i.getResponsavel(),
                        i.getConforme(),
                        i.getResistenciaAterramento(),
                        i.getContinuidadeEletrica(),
                        i.getCondicaoVisual(),
                        i.getObservacoes(),
                        i.getPdfUrl()
                ))
                .toList();

        return Optional.of(new InspecaoResponse(
                point.getCodigo(),
                point.getClienteId(),
                point.getAreaId(),
                point.getTipoPontoId(),
                point.getLocalizacao(),
                point.getDescricao(),
                point.getCriticidade(),
                point.getStatus(),
                historico
        ));
    }

    public Optional<List<Point>> points() {
        return Optional.of(pointRepository.findAll());
    }

    public Optional<Point> findByCodigo(String codigo) {
        return pointRepository.findByCodigo(codigo);
    }



    @Transactional
    public void deletePoint(String id) {
        if (!pointRepository.existsById(id)) {
            throw new RuntimeException("Ponto não encontrado com o ID: " + id);
        }
        pointRepository.deleteById(id);
    }

    @Transactional
    public RegisterResponse updatePoint(RegisterRequest register, Point point) {
        point.setCodigo(register.codigo());
        point.setLocalizacao(register.localizacao());
        point.setDescricao(register.descricao());
        point.setCriticidade(register.criticidade());
        point.setStatus(register.status());
        point.setClienteId(register.clienteId());
        point.setAreaId(register.areaId());
        point.setTipoPontoId(point.getId());

        pointRepository.save(point);
        return toResponse(point);
    }

    @Transactional
    public void patchPoint(Point point, Map<String, Object> updates) {
        updates.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(Point.class, key);
            if (field != null) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, point, value);
            }
        });
        pointRepository.save(point);
    }

    @Transactional
    public RegisterResponse cadastrarPonto(RegisterRequest register) throws Exception {
        Point point = Point.builder()
                .codigo(register.codigo() != null ? register.codigo() : gerarCodigo())
                .localizacao(register.localizacao())
                .descricao(register.descricao())
                .criticidade(register.criticidade())
                .status(register.status() != null ? register.status() : "ATIVO")
                .clienteId(register.clienteId())
                .areaId(register.areaId())
                .responsavelId(register.responsavelId())
                .build();

        pointRepository.save(point); // gera o id
        point.setTipoPontoId(point.getId());

        // QR provisório: resolve pra ficha (sem laudo ainda) ou pro último PDF (após inspeção)
        String urlResolucao = baseUrl + "/api/pontos/codigo/" + point.getCodigo() + "/redirect";
        point.setQrCodeUrl(qrCodeService.uploadQRCode(urlResolucao));

        pointRepository.save(point);
        return toResponse(point);
    }

    @Transactional
    public void atualizarAposInspecao(Point point, String urlPdf, String novoQrCodeUrl) {
        point.setUltimoPdfUrl(urlPdf);
        point.setStatus("INSPECIONADO");
        point.setQrCodeUrl(novoQrCodeUrl);
        pointRepository.save(point);
    }

    private String gerarCodigo() {
        return "PT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private RegisterResponse toResponse(Point point) {
        return new RegisterResponse(
                point.getId(),
                point.getCodigo(),
                point.getLocalizacao(),
                point.getDescricao(),
                point.getCriticidade(),
                point.getStatus(),
                point.getQrCodeUrl(),
                point.getClienteId(),
                point.getAreaId(),
                point.getTipoPontoId()
        );
    }
}
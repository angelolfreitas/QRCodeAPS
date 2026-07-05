package com.uema.qrcode.service;

import com.uema.qrcode.entity.definition.Point;
import com.uema.qrcode.entity.dto.ponto.RegisterRequest;
import com.uema.qrcode.entity.dto.ponto.RegisterResponse;
import com.uema.qrcode.infra.repository.PointRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

    @org.springframework.beans.factory.annotation.Value("${app.base-url}")
    private String baseUrl;

    public Optional<List<Point>> points(){
        return Optional.of(pointRepository.findAll());
    }

    public Optional<Point> findByCodigo(String codigo) {
        return pointRepository.findByCodigo(codigo);
    }

    @Transactional
    public void deletePoint(String id) {
        if (pointRepository.existsById(id)) {
            pointRepository.deleteById(id);
        } else {
            throw new RuntimeException("Ponto não encontrado com o ID: " + id);
        }
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

        this.pointRepository.save(point);

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
        this.pointRepository.save(point);
    }

    @Transactional
    public RegisterResponse cadastrarPonto(RegisterRequest register) throws Exception {
        Point point = Point.builder()
                .codigo(register.codigo() != null ? register.codigo() : "PT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .localizacao(register.localizacao())
                .descricao(register.descricao())
                .criticidade(register.criticidade())
                .status(register.status() != null ? register.status() : "ATIVO")
                .clienteId(register.clienteId())
                .areaId(register.areaId())
                .build();

        this.pointRepository.save(point); // gera o id

        point.setTipoPontoId(point.getId());

        // Gera o QR Code apontando pra ficha desse ponto e sobe pro S3
        String urlResolucao = baseUrl + "/api/pontos/codigo/" + point.getCodigo() + "/redirect";
        String qrCodeUrl = qrCodeService.uploadQRCode(urlResolucao);
        point.setQrCodeUrl(qrCodeUrl);

        this.pointRepository.save(point);

        return toResponse(point);
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

    @Transactional
    public void updateStatusPdf(Point point) {
        this.pointRepository.save(point);
    }
}
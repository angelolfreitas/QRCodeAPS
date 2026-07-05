package com.uema.qrcode.service;

import com.uema.qrcode.entity.definition.Inspecao;
import com.uema.qrcode.entity.definition.User;
import com.uema.qrcode.entity.dto.qr.QRCodeHistoryResponse;
import com.uema.qrcode.entity.dto.qr.QRCodeResponse;
import com.uema.qrcode.infra.port.StoragePort;
import com.uema.qrcode.infra.repository.InspecaoRepository;
import com.uema.qrcode.infra.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistryService {

    private final StoragePort storagePort;
    private final UserRepository userRepository;
    private final QRCodeService qrCodeService;
    private final InspecaoRepository inspecaoRepository;

    @Transactional
    public QRCodeResponse registrar(String txt, User user, String locationId) throws Exception {
        String awsUrl = qrCodeService.uploadQRCode(txt);

        Inspecao registry = Inspecao.builder()
                .pdfUrl(awsUrl)
                .inspetorId(user.getId())
                .pontoId(locationId)
                .build();

// 2. SALVA a inspeção no banco primeiro (ela deixa de ser transiente e ganha um ID)
        registry = inspecaoRepository.save(registry);

        User usuario = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        usuario.addRegistry(registry);

        return new QRCodeResponse(awsUrl, locationId);
    }

    @Transactional
    public void atualizarTexto(String s3Url, String novoTexto, User manager) throws Exception {
        User managedManager = userRepository.findById(manager.getId())
                .orElseThrow(() -> new RuntimeException("Manager não encontrado"));

        Inspecao antigo = managedManager.getRegistry().stream()
                .filter(r -> r.getPdfUrl().equals(s3Url))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Registro não encontrado ou não pertence a você"));

        storagePort.deleteFile(s3Url);
        managedManager.deleteRegistry(antigo);

        String novaUrl = qrCodeService.uploadQRCode(novoTexto);
        managedManager.addRegistry(Inspecao.builder()
                .pdfUrl(novaUrl)
                .inspetorId(manager.getId())
                .build());
    }

    @Transactional
    public void remover(String s3Url, User manager) {
        User managedManager = userRepository.findById(manager.getId())
                .orElseThrow(() -> new RuntimeException("Manager não encontrado"));

        Inspecao registro = managedManager.getRegistry().stream()
                .filter(r -> r.getPdfUrl().equals(s3Url))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Registro não encontrado ou não pertence a você"));

        managedManager.deleteRegistry(registro);
        storagePort.deleteFile(s3Url);
    }

    @Transactional
    public void adminRemoverEspecifico(String s3Url) {
        for (User user : userRepository.findAll()) {
            var registroOpt = user.getRegistry().stream()
                    .filter(r -> r.getPdfUrl().equals(s3Url))
                    .findFirst();

            if (registroOpt.isPresent()) {
                user.deleteRegistry(registroOpt.get());
                storagePort.deleteFile(s3Url);
                return;
            }
        }
        throw new RuntimeException("Registro não encontrado em nenhum usuário");
    }

    @Transactional
    public void adminRemoverTodos() {
        for (User user : userRepository.findAll()) {
            user.getRegistry().forEach(reg -> storagePort.deleteFile(reg.getPdfUrl()));
            user.getRegistry().clear();
        }
    }

    public List<QRCodeHistoryResponse> listarTodos() {
        return userRepository.findAllRegistriesGlobal().stream()
                .map(reg -> new QRCodeHistoryResponse(reg.getPdfUrl(), reg.getDataInspecao()))
                .collect(Collectors.toList());
    }
}
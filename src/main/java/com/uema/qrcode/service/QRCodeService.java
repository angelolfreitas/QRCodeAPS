package com.uema.qrcode.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.uema.qrcode.entity.definition.Inspecao;
import com.uema.qrcode.entity.definition.User;
import com.uema.qrcode.entity.dto.qr.QRCodeHistoryResponse;
import com.uema.qrcode.entity.dto.qr.QRCodeResponse;
import com.uema.qrcode.infra.port.StoragePort;
import com.uema.qrcode.infra.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QRCodeService {
    private final StoragePort storagePort;
    private final UserRepository userRepository;

    @Transactional
    public void updateOwnRegistryText(String s3Url, String newText, User manager) throws Exception {
        User managedManager = userRepository.findById(manager.getId())
                .orElseThrow(() -> new RuntimeException("Manager não encontrado"));

        Inspecao oldRegistry = managedManager.getRegistry().stream()
                .filter(r -> r.getPdfUrl().equals(s3Url))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Registro não encontrado ou não pertence a você"));

        storagePort.deleteFile(s3Url);
        managedManager.deleteRegistry(oldRegistry);

        String newAwsUrl = uploadQRCode(newText);

        Inspecao newRegistry = Inspecao.builder()
                .pdfUrl(newAwsUrl)
                .inspetorId(manager.getId())
                .build();

        managedManager.addRegistry(newRegistry);
    }

    @Transactional
    public void deleteOwnRegistry(String s3Url, User manager) {
        User managedManager = userRepository.findById(manager.getId())
                .orElseThrow(() -> new RuntimeException("Manager não encontrado"));

        Inspecao registryToDelete = managedManager.getRegistry().stream()
                .filter(r -> r.getPdfUrl().equals(s3Url))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Registro não encontrado ou não pertence a você"));

        managedManager.deleteRegistry(registryToDelete);
        storagePort.deleteFile(s3Url);
    }

    @Transactional
    public void adminDeleteSpecificRegistry(String s3Url) {
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            var registryOpt = user.getRegistry().stream()
                    .filter(r -> r.getPdfUrl().equals(s3Url))
                    .findFirst();

            if (registryOpt.isPresent()) {
                user.deleteRegistry(registryOpt.get());
                // O Hibernate gerencia a sincronização automaticamente graças ao @Transactional
                storagePort.deleteFile(s3Url);
                return;
            }
        }
        throw new RuntimeException("Registro não encontrado em nenhum usuário");
    }

    @Transactional
    public void adminDeleteAllRegistries() {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            // Corrigido: Agora deleta também todos os arquivos do bucket do S3 de forma síncrona
            user.getRegistry().forEach(reg -> storagePort.deleteFile(reg.getPdfUrl()));
            user.getRegistry().clear();
        }
    }

    @Transactional
    public QRCodeResponse registrateAndGenerateQRCode(String txt, User user, String locationId) throws IOException, WriterException {
        String awsUrl = uploadQRCode(txt);

        Inspecao registry = Inspecao.builder()
                .pdfUrl(awsUrl)
                .inspetorId(user.getId())
                .pontoId(locationId)
                .build();

        User newUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        newUser.addRegistry(registry);

        return new QRCodeResponse(awsUrl, locationId);
    }

    public String uploadQRCode(String txt) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(txt, BarcodeFormat.QR_CODE, 200, 200);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();

        return storagePort.uploadFile(pngData, UUID.randomUUID().toString(), "image/png");
    }

    public List<QRCodeHistoryResponse> getAllRegisteredQRCodes() {
        return userRepository.findAllRegistriesGlobal().stream()
                .map(reg -> new QRCodeHistoryResponse(reg.getPdfUrl(), reg.getDataInspecao()))
                .collect(Collectors.toList());
    }
}
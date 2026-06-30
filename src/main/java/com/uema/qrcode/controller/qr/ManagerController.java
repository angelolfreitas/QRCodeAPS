package com.uema.qrcode.controller.qr;

import com.google.zxing.WriterException;
import com.uema.qrcode.entity.definition.Registry;
import com.uema.qrcode.entity.definition.User;
import com.uema.qrcode.entity.dto.qr.QRCodeRequest;
import com.uema.qrcode.entity.dto.qr.QRCodeResponse;
import com.uema.qrcode.service.QRCodeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/qr/manager")
@AllArgsConstructor

@PreAuthorize("hasRole('MANAGER')")
public class ManagerController {

    QRCodeService qrCodeService;

    @PostMapping
    public ResponseEntity<QRCodeResponse> qrcodeRequest(
            @RequestBody QRCodeRequest request,
            @AuthenticationPrincipal User user){

        try {
            QRCodeResponse response =  qrCodeService.registrateAndGenerateQRCode(request.text(), user, request.locationId());
            return  ResponseEntity.ok(response);
        } catch (WriterException | IOException ignored) {
            //erro interno: 500, pode ser causado tanto pela má formatação
            //quanto pelas permissões limitadas na política do aws
            return ResponseEntity.internalServerError().build();
        }

    }

    @DeleteMapping
    public ResponseEntity<Void> deleteOwnRegistry(
            @RequestParam String s3Url,
            @AuthenticationPrincipal User user) {
        try {
            qrCodeService.deleteOwnRegistry(s3Url, user);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping
    public ResponseEntity<Void> updateOwnRegistry(
            @RequestParam String s3Url,
            @RequestBody QRCodeRequest request,
            @AuthenticationPrincipal User user) {
        try {
            qrCodeService.updateOwnRegistryText(s3Url, request.text(), user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}

package com.uema.qrcode.controller.qr;

import com.google.zxing.WriterException;
import com.uema.qrcode.entity.definition.User;
import com.uema.qrcode.entity.definition.role.Role;
import com.uema.qrcode.entity.dto.qr.QRCodeRequest;
import com.uema.qrcode.entity.dto.qr.QRCodeResponse;
import com.uema.qrcode.entity.dto.user.RegisterRequest;
import com.uema.qrcode.entity.dto.user.RegisterResponse;
import com.uema.qrcode.service.AuthenticationService;
import com.uema.qrcode.service.QRCodeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/qr/admin")

@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private QRCodeService qrCodeService;

    private AuthenticationService authenticationService;

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
    public ResponseEntity<Void> deleteSpecificRegistry(@RequestParam String s3Url) {
        try {
            qrCodeService.adminDeleteSpecificRegistry(s3Url);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllRegistries() {
        qrCodeService.adminDeleteAllRegistries();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sign-up")
    ResponseEntity<RegisterResponse> signUp(@RequestBody RegisterRequest registerRequest) {
        Optional<RegisterResponse> registerResponse = authenticationService.register(registerRequest,
                registerRequest.role());
        return registerResponse.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }


}

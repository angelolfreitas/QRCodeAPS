package com.uema.qrcode.controller.qr;

import com.google.zxing.WriterException;
import com.uema.qrcode.entity.definition.User;
import com.uema.qrcode.entity.definition.role.Role;
import com.uema.qrcode.entity.dto.qr.QRCodeHistoryResponse;
import com.uema.qrcode.entity.dto.qr.QRCodeRequest;
import com.uema.qrcode.entity.dto.qr.QRCodeResponse;
import com.uema.qrcode.entity.dto.user.UserResume;
import com.uema.qrcode.service.AuthenticationService;
import com.uema.qrcode.service.QRCodeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController

@RequestMapping("/api/usuarios")
@AllArgsConstructor

@PreAuthorize("hasRole('USER')")
public class UserController {
    private final QRCodeService qrCodeService;

    private final AuthenticationService authenticationService;
    @GetMapping("/qrcodes")
    public ResponseEntity<List<QRCodeHistoryResponse>> getAllUserQRCodes(@AuthenticationPrincipal User user) {

        List<QRCodeHistoryResponse> qrCodes = qrCodeService.getAllRegisteredQRCodes();

        return ResponseEntity.ok(qrCodes);
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('manager::write')")
    public ResponseEntity<List<UserResume>> listarInspetores() {
        return ResponseEntity.ok(authenticationService.listarPorRole(Role.USER)); // ou o nome do role de inspetor
    }
}

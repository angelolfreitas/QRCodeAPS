package com.uema.qrcode.entity.dto.qr;

import java.time.LocalDateTime;

public record QRCodeHistoryResponse(
        String url,
        LocalDateTime dataRegistro
) {}

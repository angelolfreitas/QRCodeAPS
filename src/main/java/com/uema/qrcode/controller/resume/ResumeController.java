package com.uema.qrcode.controller.resume;

import com.uema.qrcode.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ResumeController {

    private final PointService  pointService;

    @GetMapping("/count/points/verificados")
    public ResponseEntity<Long> countVerificados(){
        return ResponseEntity.ok(pointService.getVerifiedPoints());

    }
    @GetMapping("/count/points/nao-verificados")
    public ResponseEntity<Long> countNaoVerificados(){
        return ResponseEntity.ok(pointService.getUnverifiedPoints());

    }
    @GetMapping("/count/points")
    public ResponseEntity<Long> totalPoints(){
        return ResponseEntity.ok(pointService.getTotalPoints());

    }



}

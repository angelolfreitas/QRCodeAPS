package com.uema.qrcode.infra.repository;

import com.uema.qrcode.entity.definition.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, String> {
    Optional<Point> findByCodigo(String codigo);

    @Query("SELECT COUNT(p) FROM Point p WHERE p.status = 'VERIFICADO'")
    Long countVerificados();

    @Query("SELECT COUNT(p) FROM Point p WHERE p.status != 'VERIFICADO'")
    Long countUnverificados();
}

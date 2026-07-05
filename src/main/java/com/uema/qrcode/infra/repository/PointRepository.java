package com.uema.qrcode.infra.repository;

import com.uema.qrcode.entity.definition.Point;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, String> {
    Optional<Point> findByCodigo(String codigo);
}

package com.uema.qrcode.infra.repository;

import com.uema.qrcode.entity.definition.Inspecao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspecaoRepository extends JpaRepository<Inspecao, String> {
    List<Inspecao> findByPontoIdOrderByDataInspecaoDesc(String pontoId);
}